package org.soton.peleus.act.planner.ndcpces;

import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import org.apache.logging.log4j.core.pattern.LiteralPatternConverter;
import org.soton.peleus.act.planner.*;
import org.soton.peleus.act.planner.ndcpces.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author  meneguzz
 */
public class NDCPCESPlannerConverter implements PlannerConverter {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PlannerConverter.class.getName());
	
	protected GoalState goalState;
	
	protected StartState startState;

	protected List<List<Literal>> possibilities;
	
	protected ProblemOperators operators;
	
	protected ProblemObjects objects;
	
	protected Plan plan;

	protected String planName;

	protected int planNumber = 0;

	protected Set<String> numericSymbols;

	private Set<Literal> alwaysObserved;
	private Map<String,Set<Literal>> partiallyObserved;

	private Set<Literal> alwaysObservedRaw;
	private Map<String,Set<Literal>> partiallyObservedRaw;

	//Maps (atx(?) : atx(x_?))
	private Map<String, String> numericSymbolMap;

	@Override
	public void createPlanningProblem(List<Literal> beliefs, List<Plan> plans, List<Term> goals, List<List<Literal>> possibilities) {
		this.objects = new ProblemObjectsImpl();
		//this.startState = new StartStateImpl(this);
		this.goalState = new GoalStateImpl();
		this.numericSymbolMap = new HashMap<>();
		this.possibilities = new ArrayList<>();


		//this.plan = new ArrayList<>();
		List<Literal> tempStartState = new ArrayList<>();

		for (Iterator<Term> iter = goals.iterator(); iter.hasNext();) {
			Term term = iter.next();
			this.goalState.addTerm(term);
		}

		for (Literal literal : beliefs) {
			if(literal.getFunctor().startsWith("object")) {
				Term newTerm = DefaultTerm.parse(literal.getTerm(0)+"("+literal.getTerm(1)+")");
				this.objects.addTerm(newTerm);
			}
		}

		this.operators = new ProblemOperatorsImpl(this);

		// logger.info("Plans found: ");
		for (Plan plan : plans) {
			this.operators.add(plan);
			// logger.info(plan.toString());
		}


		System.out.println("OBJECTS: " + this.objects.getTerms());
		System.out.println("BELIEFS: " + beliefs);

		for(List<Literal> p: possibilities){
			this.possibilities.add(convertNumbers(p));
		}
		System.out.println("TESTTESTTEST2: " + this.possibilities);


		/*
		for(List<Literal> possibility : this.possibilities){
			for (Literal literal : possibility) {
				//Dont want to add objects
				if(!literal.getFunctor().startsWith("object") && literal.hasTerm()) {
					//Case for literals with terms
					//This checks the objects and makes sure that the terms in the initial values match the objects
					boolean isValidInitialValue = true;
					for (Term term : literal.getTerms()){
						if((term.isNumeric() && beliefs.stream().filter(b -> b.toString().contains("range(" + literal.getFunctor())).count() == 0)
						 || (!term.isNumeric() && !this.objects.getTerms().stream().map(t -> ((Literal)t).getTerm(0)).collect(Collectors.toList()).contains(term))) {
							isValidInitialValue = false;
							possibility.remove(literal);
							System.out.println("INVALID BELIEF: " + literal);
						}
					}
				}
			}
		}*/

		//Transforming Worlds into square versions
		// NDCPCES Can't handle (oneof (and ...)) so the expressions within the oneof need to be atomic
		this.alwaysObserved = new HashSet<>(this.possibilities.get(0));
		this.alwaysObservedRaw = new HashSet<>(possibilities.get(0));
		for(int i=1; i<possibilities.size(); i++){
			this.alwaysObserved.retainAll(this.possibilities.get(i));
			this.alwaysObservedRaw.retainAll(possibilities.get(i));
		}
		System.out.println("FULLY OBSERVABLE BELIEFS: " + this.alwaysObserved);
		System.out.println("FULLY OBSERVABLE BELIEFS RAW: " + this.alwaysObservedRaw);


		this.partiallyObserved = new HashMap<>();
		for(Literal lit : this.possibilities.get(0)){
			if(!this.alwaysObserved.contains(lit)){
				Set<Literal> temp = new HashSet<>();
				temp.add(lit);
				this.partiallyObserved.put(lit.getFunctor(), temp);
			}
		}

		this.partiallyObservedRaw = new HashMap<>();
		for(Literal lit : possibilities.get(0)){
			if(!this.alwaysObserved.contains(lit)){
				Set<Literal> temp = new HashSet<>();
				temp.add(lit);
				this.partiallyObservedRaw.put(lit.getFunctor(), temp);
			}
		}

		for(int i=1; i<this.possibilities.size(); i++){
			for(Literal lit : this.possibilities.get(i)){
				if(!this.alwaysObserved.contains(lit)){
					this.partiallyObserved.get(lit.getFunctor()).add(lit);
				}
			}
		}

		for(int i=1; i<possibilities.size(); i++){
			for(Literal lit : possibilities.get(i)){
				if(!this.alwaysObservedRaw.contains(lit)){
					this.partiallyObservedRaw.get(lit.getFunctor()).add(lit);
				}
			}
		}

		System.out.println("UNIQUE CASES: " + this.partiallyObserved);
		System.out.println("UNIQUE CASES RAW: " + this.partiallyObservedRaw);



		this.numericSymbols = new HashSet<>();

		System.out.println(tempStartState);

		//for(Literal literal : convertToRanges(tempStartState)) {
		//	this.startState.addTerm(literal);
		//}

		System.out.println("POSSIBILITIES: " + this.possibilities);
		this.planName = "plan" + planNumber;
		planNumber++;

	}

	/**
	 * @return  the goalState
	 * @uml.property  name="goalState"
	 */
	public GoalState getGoalState() {
		return goalState;
	}

	/**
	 * @return  the startState
	 * @uml.property  name="startState"
	 */
	public StartState getStartState() {
		return startState;
	}

	public ProblemOperators getProblemOperators() {
		return operators;
	}

	public ProblemObjects getProblemObjects() {
		return objects;
	}

	private List<Literal> convertNumbers(List<Literal> beliefs){
		List<Literal> ret = new ArrayList<>();
		for(Literal literal : beliefs){
			if(!literal.hasTerm()){
				ret.add(literal);
			} else{
				if(this.numericSymbolMap.containsKey(literal.toString().replaceAll("\\d+", "?"))){

					String temp = this.numericSymbolMap.get(literal.toString().replaceAll("\\d+", "?"));
					for(Term t : literal.getTerms()){
						//System.out.println("OLD LIT: " + temp);
						//System.out.println("TEST TERM: " + t);
						temp = temp.replaceFirst("\\?", t.toString());
						//System.out.println("NEW LIT: " + temp);
					}
					ret.add(Literal.parseLiteral(temp));

				} else if(literal.getTerm(0).isNumeric()) {
					for(Plan op : this.operators.getPlans()){
						PlanBody body = op.getBody();
						Boolean flag = false;
						while(body != null){
							if(body.getBodyTerm() instanceof Literal) {
								Literal lit = (Literal)body.getBodyTerm();
								if(lit.getFunctor().equals(literal.getFunctor())) {

									String tempTerm = lit.getFunctor() + "(";
									for(Term t : lit.getTerms()){
										tempTerm += t.toString().replaceAll("\\d+", "?") + ",";
									}
									tempTerm = tempTerm.substring(0, tempTerm.length() - 1);
									tempTerm += ")";
									this.numericSymbolMap.put(literal.toString().replaceAll("\\d+", "?"), tempTerm);

									String temp = this.numericSymbolMap.get(literal.toString().replaceAll("\\d+", "?"));
									for(Term t : literal.getTerms()){
										temp = temp.replaceFirst("\\?", t.toString());
									}
									ret.add(Literal.parseLiteral(temp));

									flag = true;
									System.out.println("BREAKING__: " + this.numericSymbolMap);
									break;
								}
							}
							body = body.getBodyNext();

						}
						if(flag) break;
					}
				}

			}
		}
		return ret;
	}
	/**
	 * Converts beliefs such as x(1), x(2), x(3), ~x(1), ~x(2), ~x(3) into oneof(x, x_1, x_3).
	 * Requires predicates in the actions which follow same name, like x(x_1).
	 *
	 * Current constraint: ranged beliefs must only have 1 term
	 */

	//DO NOT USE - OUTDATED
	private List<Literal> convertToRanges(List<Literal> beliefs) {
		List<Literal> positivePredicates = new ArrayList<>();
		List<Literal> negativePredicates = new ArrayList<>();

		List<Literal> returnValues = new ArrayList<>(beliefs);

		Map<String, List<Integer>> ranges = new HashMap<>();

		for(Literal literal : beliefs) {
			if(literal.toString().startsWith("~")) negativePredicates.add(Literal.parseLiteral(literal.toString().replaceFirst("~", "")));
			else positivePredicates.add(literal);
		}
		System.out.println("Beliefs befire removal: " + returnValues);


		for (Literal predicate : negativePredicates) {
			if(predicate.getTerms().size() == 1) {
				int num;
				try {
					num = Integer.parseInt(predicate.getTerm(0).toString());
				} catch(NumberFormatException e){
					continue;
				}
				if(positivePredicates.contains(predicate)) {
					if(!ranges.containsKey(predicate.getFunctor())) {
						ranges.put(predicate.getFunctor(), new ArrayList<>());
					}
					ranges.get(predicate.getFunctor()).add(num);

					returnValues.remove(predicate);
					returnValues.remove(Literal.parseLiteral("~" + predicate));
				}
			}
		}

		System.out.println("Beliefs after removal: " + returnValues);

		Map<String, String> numericSymbolMapping = new HashMap<>();
		for(String key : ranges.keySet()){
			numericSymbolMapping.put(key, "");
		}
		boolean flag = false;
		for(Plan op : this.operators.getPlans()){
			PlanBody body = op.getBody();
			while(body != null){
				if(body.getBodyTerm() instanceof Literal) {
					Literal lit = (Literal)body.getBodyTerm();
					if(ranges.containsKey(lit.getFunctor()) && lit.getTerms().size() == 1) {
						if(numericSymbolMapping.containsKey(lit.getFunctor())) {
							numericSymbolMapping.put(lit.getFunctor(), lit.getTerm(0).toString().replaceAll("\\d+", "?"));
						}
					}
				}
				if(!numericSymbolMapping.values().contains("")){
					flag = true;
					break;
				}
				body = body.getBodyNext();
			}
			if(flag) break;
		}


		for(String key : ranges.keySet()){
			Literal ret = Literal.parseLiteral( "oneof");
			ret.addTerm(Literal.parseLiteral(key));
			for(int num : ranges.get(key)){
				ret.addTerm(Literal.parseLiteral(numericSymbolMapping.get(key).replaceFirst("\\?", Integer.toString(num))));
				this.numericSymbols.add(numericSymbolMapping.get(key).replaceFirst("\\?", ""));
			}
			returnValues.add(ret);
		}
		System.out.println("Beliefs after creating oneOfs:" + returnValues);
		return returnValues;
	}

	//@Override
	public boolean executePlanner(ProblemObjects objects, StartState startState, GoalState goalState, ProblemOperators operators) {
		return executePlanner(objects, startState, goalState, operators, 10);
	}

	@Override
	public boolean executePlanner(ProblemObjects objects, StartState startState, GoalState goalState, ProblemOperators operators, int maxPlanSteps) {
		AgentSpeakToPDDL pddlCreator = new AgentSpeakToPDDL();

		pddlCreator.generatePONDPDDL(objects, this.alwaysObserved, this.partiallyObserved, goalState, operators);
		try {

			double startTime = System.currentTimeMillis();

			String[] command = new String[]{
					"ndcpces", "-o" , "domain.pddl", "-f" , "task.pddl"
			};
			Process proc = new ProcessBuilder(command).start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String policy = "";
			String line = "";
			while ((line = reader.readLine()) != null) {
				policy += line + "\n";
				System.out.println(line + "\n");
			}
			// Reading the error output
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			String errorLine = "";
			while ((errorLine = errorReader.readLine()) != null) {
				logger.warning("***NDCPCES TRANSLATION ERROR***: " + errorLine);
			}
			proc.waitFor();

			double afterPlanner = System.currentTimeMillis();

			parseNDCPCES(policy);

			double afterParse = System.currentTimeMillis();

			System.out.println("Planning Time: " + (afterPlanner-startTime));
			System.out.println("Plan Generation Time: " + (afterParse-afterPlanner));

		} catch (IOException | InterruptedException e){
			logger.warning("IOException: " + e.getMessage());
			return false;
		}



		return true;
	}

	public void parseNDCPCES(String policy){
		Trigger trigger = new Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, Literal.parseLiteral(planName));


		String contextString = "";
		for(Term b : alwaysObservedRaw){
			contextString += b.toString() + " & ";
		}
		for(Set<Literal> set : partiallyObservedRaw.values()){
			for(Literal b : set){
				contextString += "poss(" + b.toString() + ") & ";
			}
		}
		LogicalFormula context;
		try {
			context = ASSyntax.parseFormula(contextString.substring(0, contextString.length() - 3));
		}catch(ParseException e){
			System.out.println(e.getMessage());
			return;
		}

		String[] vals = policy.split("Problem Solvable")[1].split("\\[")[1].split("]")[0].split(", ");
		PlanBody prev = null;
		PlanBody start = null;
		for(String val : vals){
			PlanBody body = new PlanBodyImpl(PlanBody.BodyType.action, Literal.parseLiteral(val.replace("'", "")));
			if(prev == null){
				start = body;
				prev = body;
			} else {
				prev.add(body);
				prev = body;
			}
		}
		String label = "Generated"+planNumber;
		this.plan = new Plan(new Pred(label), trigger, context, start);
		System.out.println("PLAN CREATED: " + this.plan);
		//System.out.println("PLAN: " + plan);
	}
	
	public boolean executePlanner(ProblemObjects objects,
			StartState startState, GoalState goalState,
			ProblemOperators operators, int maxPlanSteps, long timeout)
			throws TimeoutException {
		// TODO Auto-generated method stub
		return false;
	}
	
	public StripsPlan getStripsPlan() {
		return null;
	}

	public Plan getAgentSpeakPlan(boolean generic) {

		return this.plan;
	}
	
	public String toStripsString(Literal literal) {
		StringBuffer sbTerm = new StringBuffer();
		
		if(literal.negated()) {
			sbTerm.append("-");
		}
		sbTerm.append(toStripsString((Term)literal));
		
		return sbTerm.toString();
	}
	
	public String toStripsString(Term term) {
		StringBuffer sb = new StringBuffer();
		
		if(term.isVar()) {
			sb.append("?");
		}
		
		if(term.isStructure()) {
			Structure structure = (Structure) term;
			sb.append(structure.getFunctor());
			if(structure.getArity() > 0) {
				sb.append("(");
				for (Term termPar : structure.getTermsArray()) {
					if(sb.charAt(sb.length()-1) != '(')
						sb.append(", ");
					sb.append(toStripsString(termPar));
				}
				sb.append(")");
			}
		} else {
			sb.append(term.toString());
		}
		
		
		
		return sb.toString();
	}
	
	public String toStripsString(RelExpr expr) {
		StringBuffer sb = new StringBuffer();
		
		sb.append(toStripsString2(expr.getLHS()));
		sb.append(toStripsOperator(expr.getOp()));
		sb.append(toStripsString2(expr.getRHS()));
		
		return sb.toString();
	}

	//@Override
	public List<Plan> getContingencyPlan() {
		return List.of();
	}

	public String toStripsString(PlanBody planBody) {
		StringBuffer sb = new StringBuffer();
		
		Literal literal = (Literal) planBody.getBodyTerm();
		
		sb.append(toStripsString2(literal));
		
		return sb.toString();
	}
	
	public String toStripsString2(Literal literal) {
		StringBuffer sb = new StringBuffer();
		
		if(literal.negated()) {
			sb.append("-");
		}
		sb.append(literal.getFunctor());
		if(literal.getArity() > 0) {
			sb.append("(");
			for (Term termPar : literal.getTermsArray()) {
				if(sb.charAt(sb.length()-1) != '(')
					sb.append(", ");
				sb.append(toStripsString2(termPar));
			}
			sb.append(")");
		}
		
		return sb.toString();
	}
	
	public String toStripsString2(Term term) {
		StringBuffer sb = new StringBuffer();
		
		if(!term.isVar()) {
			sb.append("@");
		}
		
		if(term.isVar()) {
			sb.append("?");
		}
		
		if(term.isStructure()) {
			Structure structure = (Structure) term;
			sb.append(structure.getFunctor());
			if(structure.getArity() > 0) {
				sb.append("(");
				for (Term termPar : structure.getTermsArray()) {
					if(sb.charAt(sb.length()-1) != '(')
						sb.append(", ");
					sb.append(toStripsString2(termPar));
				}
				sb.append(")");
			}
		} else {
			sb.append(term.toString());
		}
		
		return sb.toString();
	}

	public String toStripsNegatedOperator(RelExpr.RelationalOp op) {
		switch (op) {
		case unify:
		case eq:
			return toStripsOperator(RelExpr.RelationalOp.dif);
		case dif:
			return toStripsOperator(RelExpr.RelationalOp.eq);
		case gt:
			return toStripsOperator(RelExpr.RelationalOp.lte);
		case gte:
			return toStripsOperator(RelExpr.RelationalOp.lt);
		case literalBuilder:
			return toStripsOperator(RelExpr.RelationalOp.literalBuilder);
		case lt:
			return toStripsOperator(RelExpr.RelationalOp.gte);
		case lte:
			return toStripsOperator(RelExpr.RelationalOp.gt);
		case none:
		default:
			return "";
		}
	}

	public String toStripsOperator(RelExpr.RelationalOp op) {
		switch (op) {
		case dif:
			return "!=";
		case eq:
			return "=";
		case gt:
			return ">";
		case gte:
			return ">=";
		case literalBuilder:
			return "ARGH";
		case lt:
			return "<";
		case lte:
			return "<=";
		case unify:
			return "==";
		case none:
		default:
			return "";
		}
	}
	
	public String toStripsOperator(LogExpr.LogicalOp logicalOp) {
		switch (logicalOp) {
		case and:
			return "&";
		case not:
			return "!";
		case or:
			return "|";
		case none:
		default:
			return "";
		}
	}
}
