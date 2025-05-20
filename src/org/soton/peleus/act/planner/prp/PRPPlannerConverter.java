package org.soton.peleus.act.planner.prp;

import jason.asSemantics.Agent;
import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import org.soton.peleus.act.planner.*;
import org.soton.peleus.act.planner.prp.GoalStateImpl;
import org.soton.peleus.act.planner.prp.ProblemObjectsImpl;
import org.soton.peleus.act.planner.prp.ProblemOperatorsImpl;
import org.soton.peleus.act.planner.prp.StartStateImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PRPPlannerConverter implements PlannerConverter {

    private static final Logger logger = Logger.getLogger(PlannerConverter.class.getName());

    protected GoalState goalState;

    protected StartState startState;

    protected ProblemOperators operators;

    protected ProblemObjects objects;

    protected List<Plan> plans;

    protected String planName;

    protected static int planNumber = 0;


    @Override
    public void createPlanningProblem(List<Literal> beliefs, List<Plan> plans, List<Term> goals) {
        this.objects = new ProblemObjectsImpl();
        this.startState = new StartStateImpl(this);
        this.goalState = new GoalStateImpl();
        this.plans = new ArrayList<>();

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
                   if(!this.objects.getTerms().stream().map(t -> ((Literal)t).getTerm(0)).collect(Collectors.toList()).contains(term)) {
                       isValidInitialValue = false;
                   }
                }
                if(isValidInitialValue) this.startState.addTerm(literal);
            }
        }


        this.operators = new ProblemOperatorsImpl(this);

        // logger.info("Plans found: ");
        for (Plan plan : plans) {
            this.operators.add(plan);
            // logger.info(plan.toString());
        }

        this.planName = "plan" + planNumber;
        planNumber++;

    }

    @Override
    public GoalState getGoalState() {
        return goalState;
    }

    @Override
    public StartState getStartState() {
        return startState;
    }

    @Override
    public ProblemOperators getProblemOperators() {
        return operators;
    }

    @Override
    public ProblemObjects getProblemObjects() {
        return objects;
    }

    @Override
    public boolean executePlanner(ProblemObjects objects, StartState startState, GoalState goalState, ProblemOperators operators) {
        AgentSpeakToPDDL pddlCreator = new AgentSpeakToPDDL();

        try {


            pddlCreator.generatePDDL(objects, startState, goalState, operators);

            String[] command;

            command = new String[]{
                    "prp", "domain.pddl", "task.pddl", "--dump-policy", "2"
                    //,"&&", "python2",  "../PLANNERS/prp/prp-scripts/translate_policy.py"
            };

            Process proc1 = new ProcessBuilder(command).start();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc1.getErrorStream()));
            String errorLine = "";
            while ((errorLine = errorReader.readLine()) != null) {
                logger.warning("***PRP GENERATION ERROR***: " + errorLine);
            }

            proc1.waitFor();
            proc1.destroy();

            String[] command2 = new String[]{
                    "python2", "../PLANNERS/prp/prp-scripts/translate_policy.py"
            };
            Process proc = new ProcessBuilder(command2).start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String policy = "";
            String line = "";
            while ((line = reader.readLine()) != null) {
                policy += line + "\n";
                System.out.println(line + "\n");
            }
            // Reading the error output
            errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            errorLine = "";
            while ((errorLine = errorReader.readLine()) != null) {
                logger.warning("***PRP TRANSLATION ERROR***: " + errorLine);
            }
            proc.waitFor();

            parsePRP(policy);
        } catch (IOException | InterruptedException | ParseException e){
            logger.warning(e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean executePlanner(ProblemObjects objects, StartState startState, GoalState goalState, ProblemOperators operators, int maxPlanSteps) {
        return executePlanner(objects, startState, goalState, operators);
    }

    @Override
    public boolean executePlanner(ProblemObjects objects, StartState startState, GoalState goalState, ProblemOperators operators, int maxPlanSteps, long timeout) throws TimeoutException {
        return executePlanner(objects, startState, goalState, operators);
    }

    @Override
    public StripsPlan getStripsPlan() {
        return null;
    }

    @Override
    public Plan getAgentSpeakPlan(boolean generic) {
        return null;
    }



    @Override
    public String toStripsString(Literal literal) {
        return "";
    }

    @Override
    public String toStripsString(Term term) {
        return "";
    }

    @Override
    public String toStripsString(RelExpr expr) {
        return "";
    }

    @Override
    public List<Plan> getContingencyPlan() {
        return plans;
    }

    private void parsePRP(String out) throws jason.asSyntax.parser.ParseException {
        String[] policy = out.split("Policy:")[1].replaceAll("strong_negate_", "~").split("\n");
        LinkedHashMap<List<Literal>, Literal> planMapping = new LinkedHashMap<>();
        for(int i=1; i<policy.length-1; i++){
            if (policy[i].startsWith("If holds: ") && policy[i+1].startsWith("Execute")){
                List<Literal> preds = new ArrayList<>();
                String[] predStrings = policy[i].split(": ")[1].split("/");
                for(String str : predStrings){
                    preds.add(Literal.parseLiteral(str.replace("\n", "").replace("()", "").trim()));
                }
                String[] actionStrings = policy[i+1].split(": ")[1].split(" /")[0].split(" ");
                String action = actionStrings[0];
                if(actionStrings.length > 1){
                    action = actionStrings[0] + "(";

                    for(int j=1; j<actionStrings.length; j++){
                        action += actionStrings[j] + ",";
                    }
                    action = action.substring(0, action.length()-1);
                    action += ")";

                }
                planMapping.put(preds, Literal.parseLiteral(action));
            }
        }

        for(List<Literal> contextList : planMapping.keySet()){
            Trigger trigger = new Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, Literal.parseLiteral(planName));

            String contextString = "";
            for(Literal b : contextList){
                contextString += b.toString() + " & ";
            }
            LogicalFormula context = ASSyntax.parseFormula(contextString.substring(0,contextString.length()-3));


            if(planMapping.get(contextList).toString().equals("goal")){
                PlanBodyImpl body = new PlanBodyImpl(PlanBody.BodyType.action, Literal.parseLiteral(".fail"));
                Random r = new Random();

                String label = "Generated" + String.valueOf(r.nextDouble());

                jason.asSyntax.Plan p = new jason.asSyntax.Plan(new Pred(label),trigger,context,body);
                this.plans.add(p);
                continue;
            }

            Literal action = planMapping.get(contextList);
            Literal newLiteral = null;
            for(Plan op : operators.getPlans()){
                //System.out.println("ACTION LIT: "+ action + " OP LIT: " + op.getTrigger().getLiteral().getFunctor());
                if(op.getTrigger().getLiteral().getFunctor().equalsIgnoreCase(action.getFunctor())){
                    newLiteral = Literal.parseLiteral(action.getFunctor().toLowerCase().replace(op.getLabel().getFunctor().toLowerCase(), ""));
                    //System.out.println("ADDING PLAN LIT: "+ newLiteral.toString());
                    if(!op.getTrigger().getLiteral().hasTerm()){
                        continue;
                    }
                    for(int i=0; i<op.getTrigger().getLiteral().getTerms().size(); i++){
                        for(Term type: op.getLabel().getAnnots("type").getAsList()){
                            if(type instanceof Literal){
                                Literal lit = (Literal)type;
                                if(op.getTrigger().getLiteral().getTerms().get(i).toString().equals(lit.getTerm(0).toString()) && !lit.getTerm(2).toString().equals("temp")){
                                    newLiteral.addTerm(action.getTerm(i));
                                }
                            }
                        }
                    }
                    break;
                }
            }
            PlanBodyImpl body = new PlanBodyImpl(PlanBody.BodyType.action, newLiteral);
            body.add(new PlanBodyImpl(PlanBody.BodyType.achieve, Literal.parseLiteral(planName)));

            Random r = new Random();
            String label = "Generated" + String.valueOf(r.nextDouble());

            jason.asSyntax.Plan p = new jason.asSyntax.Plan(new Pred(label),trigger,context,body);
            this.plans.add(p);
        }
    }
}
