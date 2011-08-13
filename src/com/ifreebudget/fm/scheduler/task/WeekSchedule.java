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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.ifreebudget.fm.scheduler.task.constraints.Constraint;
import com.ifreebudget.fm.scheduler.task.constraints.WeekConstraint;

/**
 * Schedule describing weekly recurring tasks constraied with WeekConstraint
 * 
 * @author mjrz
 * @see WeekConstraint
 */
public class WeekSchedule extends BasicSchedule {
	private static final long serialVersionUID = 1L;

	public WeekSchedule(Date start, Date end) throws Exception {
		super(start, end);
	}

	@Override
	protected Date getNext(Calendar c1) {
		if (constraint == null)
			return super.getNext(c1);
		else {
			Date dt = c1.getTime();
			WeekConstraint dc = (WeekConstraint) constraint;
			Set<DayOfWeek> days = dc.getDays();
			List<Date> next = getNextDates(dt, days);
			if(next.size() == 0) {
				c1.add(Calendar.WEEK_OF_YEAR, getStep());
				c1.set(Calendar.DAY_OF_WEEK, dc.getFirst().getDayOfWeek());
				return c1.getTime();
			}
			c1.setTime(next.get(0));
			return c1.getTime();
		}
	}

	private List<Date> getNextDates(Date date, Set<DayOfWeek> days) {
		List<Date> dates = new ArrayList<Date>();
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date);
		
		for(DayOfWeek d : days) {
			c1.set(Calendar.DAY_OF_WEEK, d.getDayOfWeek());
			if(c1.getTime().after(date)) {
				dates.add((Date) c1.getTime());
				break;
			}
		}
		return dates;
	}
	
	public List<DayOfWeek> getFirst(Calendar cal, Set<DayOfWeek> days) {
		Calendar clone = (Calendar) cal.clone();
		List<DayOfWeek> ret = new ArrayList<DayOfWeek>();
		for(DayOfWeek d : days) {
			clone.set(Calendar.DAY_OF_WEEK, d.getDayOfWeek());
			if(clone.before(cal)) {
				continue;
			}
			ret.add(d);
		}
		return ret;
	}
	
	private void adjustDate(Calendar c1) {
		WeekConstraint dc = (WeekConstraint) constraint;
		List<DayOfWeek> available = getFirst(c1, dc.getDays());
		if(available.size() == 0) {
			c1.add(Calendar.WEEK_OF_YEAR, getStep());
			c1.set(Calendar.DAY_OF_WEEK, dc.getFirst().getDayOfWeek());
		}
		else {
			DayOfWeek next = available.get(0);
			c1.set(Calendar.DAY_OF_WEEK, next.getDayOfWeek());			
		}
		this.start = c1.getTime();
	}

	@Override
	public void setConstraint(Constraint constraint) {
		if (! (constraint instanceof WeekConstraint) ) {
			throw new IllegalArgumentException("Invalid constraint");
		}
		this.constraint = constraint;
		Calendar c = Calendar.getInstance();
		c.setTime(start);
		adjustDate(c);
	}
}
