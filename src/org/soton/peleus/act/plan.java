// Internal action code for project Argos.mas2j

package org.soton.peleus.act;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.input.ReversedLinesFileReader;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Plan;
import jason.pl.PlanLibrary;
import jason.asSyntax.Pred;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.bb.BeliefBase;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.parser.ParseException;

import java.util.*;
import java.util.logging.Logger;

import org.soton.peleus.act.planner.*;
import org.soton.peleus.act.planner.javagp.JavaGPPlannerConverter;
import org.soton.peleus.act.planner.jemplan.EMPlanPlannerConverter;
import org.soton.peleus.act.planner.jplan.JPlanPlannerConverter;
import org.soton.peleus.act.planner.prp.PRPPlannerConverter;
import org.soton.peleus.act.planner.ndcpces.NDCPCESPlannerConverter;


/**
 * An <code>InternalAction</code> that links an AgentSpeak agent to
 * an external planning module. This action converts specially designed 
 * plans in the agent's <code>PlanLibrary</code> into a planning problem 
 * to create a new high-level plan. This plan is then added to the plan
 * library and adopted as a new intention by the agent.
 *
 * @author  Felipe Meneguzzi
 */
@SuppressWarnings("serial")
public class plan extends DefaultInternalAction {
	protected PlannerConverter plannerConverter;

	protected static final Term trueTerm = Pred.LTrue;
	protected static final Term remote = ASSyntax.createAtom("remote");

	protected int planNumber = 0;

	protected List<Term> mixedActions;

	//@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(InternalAction.class.getName());

	/**
	 * Default constructor
	 */
	public plan() {
		//plannerConverter = createPlannerConverter("emplan");
		plannerConverter = createPlannerConverter("jplan"); //javagp
	}

	public boolean suspendIntention() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Returns the regular expression that selects the plans
	 * that will be used in composing a new plan
	 * @return
	 */
	protected String getPlanSelector() {
		return "action(.)*";
	}

	/**
	 * Instantiates a planner converter based on the supplied planner
	 * selection string.
	 *
	 * TODO Perhaps I should do this instantiation using reflection.
	 *
	 * @param plannerName
	 * @return
	 */
	protected PlannerConverter createPlannerConverter(String plannerName) {
		PlannerConverter converter = null;

		if(plannerName.equals("emplan")) {
			converter = new EMPlanPlannerConverter();
		} else if (plannerName.equals("jplan")) {
			converter = new JPlanPlannerConverter();
		} else if (plannerName.equals("javagp")) {
			converter = new JavaGPPlannerConverter();
		} else if (plannerName.equals("prp")){
			converter = new PRPPlannerConverter();
		} else if (plannerName.equals("ndcpces")){
			converter = new NDCPCESPlannerConverter();
		} else {
			converter = new JavaGPPlannerConverter();
		}

		return converter;
	}

	/**
	 * Ignore plans that are not marked with the action annotation
	 * @param plans
	 * @param useRemote TODO
	 * @return
	 */
	protected List<Plan> selectUseablePlans(List<Plan> plans, boolean useRemote) {
		plans = ProblemOperators.getLabelledPlans(plans, getPlanSelector());
		for(Plan plan : plans) {
			if(!useRemote && plan.getLabel().getAnnots().contains(remote)) {
				plans.remove(plan);
			}
		}
		return plans;
	}

	/**
	 * Extracts the literals in the belief base to be used
	 * as the initial state for the planning problem
	 *
	 * @param beliefBase
	 * @return
	 */
	protected List<Literal> selectRelevantBeliefs(BeliefBase beliefBase) {
		Iterator<Literal> beliefsIterator = beliefBase.iterator();
		List<Literal> beliefs = new ArrayList<Literal>();
		while(beliefsIterator.hasNext()) {
			//Modified to filter out perceptions and rely only on beliefs
			Literal belief = beliefsIterator.next();
			//if(belief.getAnnots().contains(BeliefBase.TSelf)) {
			beliefs.add(belief);
			//}
		}
		return beliefs;
	}

	/**
	 * Invokes the external planner.
	 * @param beliefs
	 * @param goals
	 * @param plans
	 * @param maxPlanSteps
	 * @return
	 */
	protected boolean invokePlanner(List<Literal> beliefs,
									List<Term> goals,
									List<Plan> plans,
									List<List<Literal>> multipleWorlds,
									int maxPlanSteps) {
		plannerConverter.createPlanningProblem(beliefs, plans, goals, multipleWorlds, this.planNumber);

		ProblemObjects objects = plannerConverter.getProblemObjects();
		StartState startState = plannerConverter.getStartState();
		GoalState goalState = plannerConverter.getGoalState();
		ProblemOperators operators = plannerConverter.getProblemOperators();

		//Invoke the planner with the generated planning problem
		return plannerConverter.executePlanner(objects, startState, goalState, operators, maxPlanSteps);
	}

	/**
	 * Converts the last plan into an AgentSpeak representation
	 * And add the appropriate modifications to it.
	 * @param makeGeneric	Whether or not the plan should be made generic for reuse
	 * @param makeAtomic	Whether or not the plan should be make atomic
	 * @return
	 */
	protected Plan convertPlan(boolean makeGeneric, boolean makeAtomic, boolean makeRemote) {

		logger.info("convertPlan("+makeGeneric+","+makeAtomic+","+makeRemote+")");

		Plan plan = plannerConverter.getAgentSpeakPlan(makeGeneric);

		String atomic = "";
		String remote = "";

		if(makeAtomic) {
			atomic = "atomic";
			if(makeRemote) {
				atomic+=",";
			}
		}

		if(makeRemote) {
			remote = "remote";
		}

		logger.info("convertPlan: "+atomic+", "+remote);

		plan.setLabel(Pred.parsePred(plan.getTrigger().getLiteral().getTerm(0)+"[atomic]"));
		//plan.setLabel(Pred.parsePred("plan"+(planNumber++)+"["+atomic+remote+"]"));

		return plan;
	}

	/**
	 * Adds the new plan to the intention structure to execute it
	 * @param plan
	 * @param ts
	 * @throws JasonException
	 */
	protected void executeNewPlan(Plan plan, TransitionSystem ts) throws JasonException {
		logger.info("Adding new plan: "+System.getProperty("line.separator")+plan);
		ts.getAg().getPL().add(plan,true);

		logger.info("Invoking plan "+planNumber);
		ts.getC().addAchvGoal(Literal.parseLiteral("plan"+planNumber+""), null);
		// Now we are adding the new goal to the current intention
		//ts.getC().addAchvGoal(trigger.getLiteral(), ts.getC().getSelectedIntention());
		planNumber++; //TODO: Convert and execute must be done in sequence
	}

	protected Plan generateMixedPlan(Term action, List<List<Literal>> multiWorldInits){

		Trigger trigger = new Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, Literal.parseLiteral("plan" + planNumber));

		Set<Literal> alwaysObserved = new HashSet<>(multiWorldInits.get(0));
		Set<Literal> partiallyObserved = new HashSet<>(multiWorldInits.get(0));
		for(int i=1; i<multiWorldInits.size(); i++){
			alwaysObserved.retainAll(multiWorldInits.get(i));
			partiallyObserved.addAll(multiWorldInits.get(i));
		}

		String ctx = "";
		for(Literal lit : alwaysObserved){
			partiallyObserved.remove(lit);
			ctx += lit.toString() + " & ";
		}

		for(Literal lit : partiallyObserved){
			ctx += "poss(" + lit.toString() + ") & ";
		}
		ctx += "desires(Goal)";
		LogicalFormula context;
		try{
			context = ASSyntax.parseFormula(ctx);
		} catch (ParseException e){
			logger.warning("ERROR: " + e);
			return null;
		}

		PlanBody actionBody = new PlanBodyImpl(PlanBody.BodyType.action,action);
		actionBody.add(new PlanBodyImpl(PlanBody.BodyType.internalAction, Literal.parseLiteral("org.soton.peleus.act.plan(Goal, [mixed(true)])")));

		String label = "Generated"+planNumber;

		return new Plan(new Pred(label), trigger, context, actionBody);
	}

	/**
	 * Adds the new plan to the intention structure to execute it
	 * @param plans
	 * @param ts
	 * @throws JasonException
	 */
	protected void executeNewContingencyPlan(List<Plan> plans, TransitionSystem ts) throws JasonException {
		Collections.reverse(plans);
		for(Plan plan : plans){
			logger.info("Adding new plan: "+System.getProperty("line.separator")+plan);
			ts.getAg().getPL().add(plan,true);
		}
		ts.getC().addAchvGoal(Literal.parseLiteral("plan"+planNumber), null);
		planNumber++;
	}
	//@SuppressWarnings("unchecked")
	public Object execute(TransitionSystem ts, Unifier un, Term[] args)
			throws Exception {

		Thread.sleep(5000);

		//double startTime = System.currentTimeMillis();
		System.gc();
		double startTime = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024.0/1024.0;


		logger.info("args[0]: "+args[0]+" / args[1]: "+args[1]);
		//First check that the action was properly invoked with an AgentSpeak
		//list as its parameter.
		if(args.length < 1) {
			logger.info("plan action must have at least one parameter");
			return false;
		}
		if(!(args[0] instanceof ListTerm)){
			logger.info("plan action requires a list of literals as its parameter");
			return false;
		}

		//The second optional parameter is a list of options for the
		//planner
		if(args.length > 2) {
			logger.info("plan action cannot have more than thow parameters");
			return false;
		}
		if(!(args[1] instanceof ListTerm)){
			logger.info("plan action requires a list as its second parameter");
			return false;
		}

		ListTerm listTerm = (ListTerm) args[0];
		List<Term> goals = listTerm.getAsList();
		ListTerm plannerParams = (ListTerm) args[1];
		List<Term> params = plannerParams.getAsList();
		int maxPlanSteps = 50;
		boolean makeAtomic = true;
		boolean makeGeneric = true;
		//Whether or not to use remote operators
		boolean useRemote = false;
		String plannerName = null;
		boolean mixed = false;
		if(this.mixedActions == null) {
			this.mixedActions = new ArrayList<>();
		}


		for(int i=0; i<params.size(); i++) {
			Structure param = (Structure) params.get(i);
			if(param.getFunctor().equals("maxSteps")) {
				NumberTerm term = (NumberTerm) param.getTerm(0);
				maxPlanSteps = (int) term.solve();
			} else if(param.getFunctor().equals("makeGeneric")) {
				makeGeneric = Boolean.parseBoolean(param.getTerm(0).toString());
			} else if(param.getFunctor().equals("makeAtomic")) {
				makeAtomic = Boolean.parseBoolean(param.getTerm(0).toString());
			} else if(param.getFunctor().equals("useRemote")) {
				useRemote = Boolean.parseBoolean(param.getTerm(0).toString());
			} else if(param.getFunctor().equals("planner")) {
				plannerName = param.getTerm(0).toString();
			} else if(param.getFunctor().equals("mixed")) {
				mixed = Boolean.parseBoolean(param.getTerm(0).toString());
			}
		}

		//Extract the literals in the belief base to be used
		//as the initial state for the planning problem
		BeliefBase beliefBase = ts.getAg().getBB();
		List<Literal> beliefs = selectRelevantBeliefs(beliefBase);
		//logger.info("beliefBase: "+beliefBase);
		//for(Literal belief : beliefs) {
			//logger.info("BEL: _"+belief+"_");
		//}

		//Extract the plans from the plan library to generate
		//STRIPS operators in the conversion process
		PlanLibrary planLibrary = ts.getAg().getPL();
		List<Plan> plans = planLibrary.getPlans();
		List<List<Literal>> multiWorldInits = new ArrayList<>();
		plans = selectUseablePlans(plans, useRemote);
		//logger.info("planLibrary: "+planLibrary);

		for(Plan plan : plans){
			if (plan.toString().toLowerCase().contains("oneof")){
				plannerName = "prp";
				break;
			}
		}
		//double initialSetup = System.currentTimeMillis();
		System.gc();
		double initialSetup = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024.0/1024.0;


		plannerName = "ndcpces";
		String logFileName = "";
		//System.out.println(Files.walk(Paths.get("../PLANNERS/epistemic-reasoner/logs")));
		for(Path path : Files.walk(Paths.get("../PLANNERS/epistemic-reasoner/logs")).toList()){
			if(path.toString().toLowerCase().endsWith(".log")){
				logFileName = path.toString();
				//System.out.println("FILE: " + path.toString());
			}
		}
		File logFile = new File(logFileName);
		Scanner s = new Scanner(logFile);
		ReversedLinesFileReader r = new ReversedLinesFileReader(logFile);
		boolean flag = false;
		String line = "";
		do{
			line = r.readLine();
			if(line.matches("\\d*_.*")){
				flag = true;

				String[] data = line.split(" : ");
				//System.out.println("TESTEST DATA: "+data);

				List<Literal> possibility = new ArrayList<>();
				boolean outsideList = true;
				String strWorldList = data[data.length-1].replace("\n", "");


				int depth = 0;
				String curr = "";

				for (int i = 0; i < strWorldList.length(); i++) {
					char b = strWorldList.charAt(i);

				//for(String b : data[data.length-1].replace("\n", "").replaceAll("\\)", "))").split(",")){
					if(b == '[' || b == '('){
						depth++;
					} else if(b == ']' || b == ')'){
						depth--;
					}

					if (b == ',' && depth == 0) {

						if(!curr.contains("object(") && !curr.contains(":-") && !curr.contains("desires(") && !curr.contains("[") && !curr.contains("]")){
							possibility.add(Literal.parseLiteral(curr));
						}
						curr = "";
					} else {
						curr += b;
					}

					//if(!b.contains("object(") && !b.contains(":-") && !b.contains("desires(") && !b.contains("[") && !b.contains("]") && outsideList){
						//System.out.println("LITERAL POSS: " + b);
					//	possibility.add(Literal.parseLiteral(b));
					//}
				}

				if(!curr.contains("object(") && !curr.contains(":-") && !curr.contains("desires(") && !curr.contains("[") && !curr.contains("]")){
					possibility.add(Literal.parseLiteral(curr.trim()));
				}
				curr = "";

				multiWorldInits.add(possibility);
				System.out.println("POSSIBILITY: " + possibility);
			} else if(flag){
				break;
			}
		}while(line != null);
		r.close();
		/*
		while(s.hasNextLine()){
			String line = s.nextLine();
			//System.out.println(line);
			if(flag){
				String[] data = line.split(" : ");
				//System.out.println("TESTEST DATA: "+data);

				if(data.length == 1){
					flag = false;
					continue;
				}
				List<Literal> possibility = new ArrayList<>();
				boolean outsideList = true;
				for(String b : data[data.length-1].replace("\n", "").replaceAll("\\)", "))").split("\\),")){
					if(b.contains("[")){
						outsideList = false;
					} else if(b.contains("]")){
						outsideList = true;
					} if(!b.contains("object(") && !b.contains(":-") && !b.contains("desires(") && !b.contains("[") && !b.contains("]") && outsideList){
						//System.out.println("LITERAL POSS: " + b);
						possibility.add(Literal.parseLiteral(b));
					}
				}
				multiWorldInits.add(possibility);

			} else if (line.contains("RESULT :")){
				flag = true;
				multiWorldInits = new ArrayList<>();
			}
		}
		s.close();
		*/

		System.out.println("STARTING STATE: " + multiWorldInits);
		if(multiWorldInits.size() == 1){
			plannerName = "prp";
			System.out.println("USING PRP");
		} else {
			plannerName = "ndcpces";
			System.out.println("USING NDCPCES");
		}
		//System.out.println("INIT WORLDS: " + multiWorldInits);

		//double modelReading = System.currentTimeMillis();
		System.gc();
		double modelReading = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024.0/1024.0;


		//If the planner(X) parameter was specified
		//select another planner converter
		if(plannerName != null) {
			logger.info("PlannerName: "+plannerName);
			PlannerConverter converter = createPlannerConverter(plannerName);
			if(converter != null) {
				this.plannerConverter = converter;
			}
		}

		//Invoke the planner
		if(!mixed || ((plannerName.equals("ndcpces") && mixedActions.isEmpty()) || plannerName.equals("prp"))) {
			boolean planFound = invokePlanner(beliefs, goals, plans, multiWorldInits, maxPlanSteps);

			if(!planFound) {
				logger.info("Plan not Found!!!");
				return false;
			}
		}

		if(mixed && mixedActions.isEmpty() && plannerName.equals("ndcpces")){
			PlanBody curr = plannerConverter.getAgentSpeakPlan(false).getBody();
			while(curr != null){
				mixedActions.add(curr.getBodyTerm());
				curr = curr.getBodyNext();
			}
		}


		if(plannerName.equals("prp")) {
			//logger.info("Contingency plan cannot be converted.");

			executeNewContingencyPlan(plannerConverter.getContingencyPlan(), ts);
			mixedActions.clear();
		} else {
			Plan plan;
			if(mixed && !mixedActions.isEmpty() && plannerName.equals("ndcpces")) {
				Term action = mixedActions.remove(0);
				plan = generateMixedPlan(action, multiWorldInits);
			} else {
				logger.info("Converting plan...");
				plan = convertPlan(makeGeneric, makeAtomic, useRemote);
			}
			logger.info("Executing plan...");
			executeNewPlan(plan, ts);
		}

		//double endTime = System.currentTimeMillis();
		System.gc();
		double endTime = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024.0/1024.0;


		//System.out.println("TIMETAKEN INIT SETUP: " + (initialSetup - startTime));
		//System.out.println("TIMETAKEN MODEL READING: " + (modelReading - initialSetup));
		//System.out.println("TIMETAKEN TOTAL: " + (endTime - startTime));
		//System.out.println("FINAL PLAN LIBRARY: " + ts.getAg().getPL());
		//Thread.sleep(10000);
		return true;
	}

	//Never let the planner be used in the context
	public boolean canBeUsedInContext() {
		return false;
	}

}