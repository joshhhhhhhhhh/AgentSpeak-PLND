package org.soton.peleus.act.Environments;

import java.util.*;
import java.util.logging.Logger;

import jason.asSemantics.InternalAction;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.util.Random;
import java.util.logging.Logger;

    public class MAPC2019Env extends Environment {

        int x;
        int y;
        int next;
        Map<String, Boolean> obstacles;
        String moved;
        boolean start = true;

        private static final Logger logger = Logger.getLogger(InternalAction.class.getName());

        @Override
        public void init(String[] args) {
            this.x = 1;
            this.y = 1;
            this.next = 0;
            obstacles = new HashMap<String, Boolean>();
            obstacles.put("left", false);
            obstacles.put("right", false);
            obstacles.put("up", false);
            obstacles.put("down", true);
            moved = "none";
            updatePercepts();
        }

        private boolean obstacle(String direction){
            if(direction.equals("left")){
                return (x == 3 && y == 2);
            } else if(direction.equals("right")){
                return (x == 0 && y == 2);
            } else if(direction.equals("up")){
                return ( (x == 1 && y == 3) || (x == 2 && y == 3));
            } else if(direction.equals("down")){
                return ( (x == 1 && y == 1) || (x == 2 && y == 1));
            } else {
                return false;
            }
        }

        @Override
        public boolean executeAction(String agName, Structure act) {

            moved = "none";
            if(act.getFunctor().equals("move_left")) {
                if(this.x > 0 && !obstacle("left")) {
                    this.x -= 1;
                    moved = "left";
                }
            } else if(act.getFunctor().equals("move_right")) {
                if(this.x < 4 && !obstacle("right")){
                    this.x += 1;
                    moved = "right";
                }
            } else if(act.getFunctor().equals("move_up")) {
                if(this.y > 0 && !obstacle("up")) {
                    this.y -= 1;
                    moved = "up";
                }
            } else if(act.getFunctor().equals("move_down")){
                if(this.y < 4 && !obstacle("down")) {
                    this.y += 1;
                    moved = "down";
                }
            } else{
                System.out.println("WRONG ACTION: " + act.getFunctor());
            }
            for(String s : obstacles.keySet()){
                this.obstacles.put(s, obstacle(s));
            }
            //logger.info("PERCEPTS BEFORE ACTION: " + getPercepts(agName));
            logger.info(act.getFunctor() + " ACTION TAKEN");
            logger.info("OBSTACLES: " + obstacles);
            updatePercepts();
            next += 1;
            return true;
        }

        public void updatePercepts(){
            clearPercepts();
            if(!moved.equals("none")){
                addPercept(Literal.parseLiteral("moved(" + moved +", " + next + ")"));
            }
            for(String s : obstacles.keySet()){
                if(obstacles.get(s)){
                    logger.info("OBSTACLE DIR:" + s);
                    addPercept(Literal.parseLiteral("obs(" + s + ", " + next + ")"));
                } else {
                    addPercept(Literal.parseLiteral("~obs(" + s + ", " + next + ")"));
                }
            }

            for(int i=0; i<5; i++){
                addPercept(Literal.parseLiteral("object(x, x_" + i + ")"));
                addPercept(Literal.parseLiteral("object(y, y_" + i + ")"));
            }
            if(start){
                addPercept(Literal.parseLiteral("desires([posx(x_2), posy(y_3)])"));
            }
            logger.info("CURRENT LOCATION: X-" + this.x + " Y-" + this.y);
        }

    }



