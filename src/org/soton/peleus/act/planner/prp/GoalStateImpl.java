package org.soton.peleus.act.planner.prp;

import jason.asSyntax.Term;
import org.soton.peleus.act.planner.GoalState;

import java.util.Iterator;



public class GoalStateImpl extends GoalState {

	@Override
	public String toPlannerString() {
		StringBuffer sb = new StringBuffer();
		sb.append("goal: ");
		for (Iterator<Term> iter = terms.iterator(); iter.hasNext();) {
			Term term = iter.next();
			sb.append(term.toString());
			if (iter.hasNext()) {
				sb.append(" & ");
			}
		}

		return sb.toString();
	}
}