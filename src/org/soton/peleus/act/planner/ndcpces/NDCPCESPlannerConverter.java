package org.soton.peleus.act.planner.ndcpces;

import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
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
	
	protected ProblemOperators operators;
	
	protected ProblemObjects objects;
	
	protected List<Term> plan;

	protected String planName;

	protected int planNumber = 0;

	protected Set<String> numericSymbols;

	public void createPlanningProblem(List<Literal> beliefs, List<Plan> plans, List<Term> goals) {
		this.objects = new ProblemObjectsImpl();
		this.startState = new StartStateImpl(this);
		this.goalState = new GoalStateImpl();
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

		System.out.println("OBJECTS: " + this.objects.getTerms());
		System.out.println("BELIEFS: " + beliefs);
		for (Literal literal : beliefs) {
			//Dont want to add objects
			if(literal.getFunctor().startsWith("object")) {
				continue;
			}
			// Adding literals without terms
			if(!literal.hasTerm()){
				this.startState.addTerm(literal);
			} else {
				//Case for literals with terms
				//This checks the objects and makes sure that the terms in the initial values match the objects
				boolean isValidInitialValue = true;
				for (Term term : literal.getTerms()){
					if(!term.isNumeric() && !this.objects.getTerms().stream().map(t -> ((Literal)t).getTerm(0)).collect(Collectors.toList()).contains(term)) {
						isValidInitialValue = false;
						System.out.println("INVALID BELIEF: " + literal);
					}
				}
				if(isValidInitialValue) tempStartState.add(literal);
			}
		}


		this.operators = new ProblemOperatorsImpl(this);

		// logger.info("Plans found: ");
		for (Plan plan : plans) {
			this.operators.add(plan);
			// logger.info(plan.toString());
		}

		this.numericSymbols = new HashSet<>();

		System.out.println(tempStartState);

		for(Literal literal : convertToRanges(tempStartState)) {
			this.startState.addTerm(literal);
		}

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

	/**
	 * Converts beliefs such as x(1), x(2), x(3), ~x(1), ~x(2), ~x(3) into oneof(x, x_1, x_3).
	 * Requires predicates in the actions which follow same name, like x(x_1).
	 *
	 * Current constraint: ranged beliefs must only have 1 term
	 */
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
				ret.addTerm(Literal.parseLiteral(numericSymbolMapping.get(key).replace("?", Integer.toString(num))));
				this.numericSymbols.add(numericSymbolMapping.get(key).replace("?", ""));
			}
			returnValues.add(ret);
		}
		System.out.println("Beliefs after creating oneOfs:" + returnValues);
		return returnValues;
	}

	public boolean executePlanner(ProblemObjects objects, StartState startState, GoalState goalState, ProblemOperators operators) {
		return executePlanner(objects, startState, goalState, operators, 10);
	}

	@Override
	public boolean executePlanner(ProblemObjects objects, StartState startState, GoalState goalState, ProblemOperators operators, int maxPlanSteps) {
		AgentSpeakToPDDL pddlCreator = new AgentSpeakToPDDL();
		pddlCreator.generatePDDL(objects, startState, goalState, operators);
		try {



			String[] command = new String[]{
					"ndcpces", "domain.pddl", "task.pddl"
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


			parseNDCPCES(policy, this.plan);
		} catch (IOException | InterruptedException e){
			logger.warning(e.getMessage());
			return false;
		}



		return true;
	}

	public void parseNDCPCES(String policy, List<Term> outputPlan){
		outputPlan.clear();
		//TODO fill plan with the actual values
		// when going through, remove all instances of the numericSymbols.
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
		return null;
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
