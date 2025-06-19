package org.soton.peleus.act.Environments;

import jason.asSemantics.InternalAction;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class TrickyGridEnv extends Environment {

    int x;
    int y;
    boolean canMove;
    Random r;
    boolean alive;

    private static final Logger logger = Logger.getLogger(InternalAction.class.getName());

    @Override
    public void init(String[] args) {
        r = new Random();
        this.x = r.nextInt(3)+1;
        this.y = r.nextInt(3)+1;
        this.canMove = true;
        updatePercepts();
    }

    @Override
    public boolean executeAction(String agName, Structure act) {
        this.r = new Random();
        if(act.getFunctor().equals("check")){
            this.canMove = true;
        } else if(act.getFunctor().equals("move_left")) {
            if(this.x > 0) this.x -= 1;
            this.canMove = false;
        } else if(act.getFunctor().equals("move_right")) {
            if(this.x < 4) this.x += 1;
            if(r.nextDouble() < 0.50 && this.y < 4) this.y += 1;
            this.canMove = false;
        } else if(act.getFunctor().equals("move_up")) {
            if(this.y < 4) this.y += 1;
            this.canMove = false;
        } else if(act.getFunctor().equals("move_down")){
            if(this.y > 0) this.y -= 1;
            this.canMove = false;
        } else{
            System.out.println("WRONG ACTION: " + act.getFunctor());
        }
        //logger.info("PERCEPTS BEFORE ACTION: " + getPercepts(agName));
        logger.info(act.getFunctor() + " ACTION TAKEN");
        updatePercepts();
        /*
        try{
            Thread.sleep(200);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        */
        return true;
    }

    public void updatePercepts(){
        if(this.canMove) {
            addPercept(Literal.parseLiteral("can_move"));
            removePercept(Literal.parseLiteral("has_to_check"));
        } else {
            addPercept(Literal.parseLiteral("has_to_check"));
            removePercept(Literal.parseLiteral("can_move"));
        }
        if(y == 4 || (x == 0 && y == 0) || (x == 4 && y == 0)){
            removePercept(Literal.parseLiteral("alive"));
        }

        for(int i=0; i<5; i++){
            addPercept(Literal.parseLiteral("object(x, x_" + i + ")"));
            addPercept(Literal.parseLiteral("object(y, y_" + i + ")"));
        }

        addPercept(Literal.parseLiteral("desires([atx(x_2), aty(y_2)])"));
        logger.info("CURRENT ACTUAL LOCATION: X-" + this.x + " Y-" + this.y);
    }

}
