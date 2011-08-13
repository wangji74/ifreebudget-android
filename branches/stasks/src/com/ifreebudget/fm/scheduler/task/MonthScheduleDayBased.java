/*******************************************************************************
 * Copyright  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ifreebudget.fm.scheduler.task;

import java.util.Calendar;
import java.util.Date;

import com.ifreebudget.fm.scheduler.task.constraints.Constraint;
import com.ifreebudget.fm.scheduler.task.constraints.MonthConstraintDayBased;

/**
 * Schedule describing monthly recurring tasks constraied with
 * MonthConstraintDayBased
 * 
 * @author mjrz
 * @see MonthConstraintDayBased
 */
public class MonthScheduleDayBased extends BasicSchedule {
	private static final long serialVersionUID = 1L;

	public MonthScheduleDayBased(Date start, Date end) throws Exception {
		super(start, end);
	}

	@Override
	public void setStartTime(Date start) {
		this.start = start;
	}

	protected Date getNext(Calendar c1) {
		if (constraint == null)
			return super.getNext(c1);

		c1.add(Calendar.MONTH, getStep());

		MonthConstraintDayBased dc = (MonthConstraintDayBased) constraint;

		DayOfWeek dow = dc.getDayOfWeek();
		WeekOfMonth wom = dc.getWeekOfMonth();

		adjustDate(c1, wom, dow);

		return c1.getTime();
	}

	private void adjustDate(Calendar c1, WeekOfMonth wom, DayOfWeek dow) {
		int inst = wom.getWeekOfMonth();
		c1.set(Calendar.DAY_OF_MONTH, 1);
		int mon = c1.get(Calendar.MONTH);
		int occurance = 0;

		while (true) {
			int tmp = c1.get(Calendar.DAY_OF_WEEK);
			if (tmp == dow.getDayOfWeek()) {
				occurance += 1;
			}
			if (occurance >= inst)
				break;
			c1.add(Calendar.DATE, 1);
			if (mon != c1.get(Calendar.MONTH)) {
				negAdjustDate(c1, dow.getDayOfWeek());
				break;
			}
		}
		if (c1.getTime().before(new Date())) {
			c1.add(Calendar.MONTH, getStep());
			adjustDate(c1, wom, dow);
		}
	}

	private void negAdjustDate(Calendar c, int dow) {
		while (true) {
			c.add(Calendar.DATE, -1);
			if (c.get(Calendar.DAY_OF_WEEK) == dow)
				break;
		}
	}

	@Override
	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
		Calendar c1 = Calendar.getInstance();
		c1.setTime(start);
		MonthConstraintDayBased dc = (MonthConstraintDayBased) constraint;
		adjustDate(c1, dc.getWeekOfMonth(), dc.getDayOfWeek());
		this.start = c1.getTime();
	}

	@Override
	public String toString() {
		String s = super.toString();
		return s + constraint.toString();
	}
}
