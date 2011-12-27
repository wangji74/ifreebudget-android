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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ifreebudget.fm.scheduler.task.constraints.Constraint;

/**
 * Basic implementation of the <code>Schedule</code> interface. Specific
 * schedules (monthly, weekly etc,) should override the getNext() method to
 * implement the appropriate next run time calculation
 * 
 * @author mjrz
 * 
 */
public class BasicSchedule implements Schedule, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected Date start;
	protected Date end;
	private RepeatType repeatType;
	private int step;
	private Date lastRunTime;
	protected Constraint constraint;

	/**
	 * Constructs a schedule with specified start and end dates
	 * @param start Start date of the schedule
	 * @param end End date of the schedule
	 * @throws Exception
	 */
	public BasicSchedule(Date start, Date end) throws Exception {
		if (end.before(start)) {
			throw new IllegalArgumentException(
					"End date cannot be before start date.[start=" + start
							+ " , end=" + end + "]");
		}
		this.start = start;
		this.end = end;
	}

	@Override
	public void setStartTime(Date start) {
		this.start = start;
	}

	@Override
	public void setRepeatType(RepeatType type, int step) {
		this.repeatType = type;
		this.step = step;
	}

	@Override
	public RepeatType getRepeatType() {
		return repeatType;
	}

	@Override
	public int getStep() {
		return step;
	}

	@Override
	public Date getLastRunTime() {
		return lastRunTime;
	}

	@Override
	public void setLastRunTime(Date lastRunTime) {
		this.lastRunTime = lastRunTime;
	}

	@Override
	public Date getNextRunTimeAfter(Date date) {
		BasicSchedule dup = (BasicSchedule) getDuplicate();
		if(dup == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return dup.getNext(c);
	}

	@Override
	public Date getNextRunTime() {
		if (repeatType == null || repeatType == RepeatType.NONE)
			return null;

		Calendar c = Calendar.getInstance();
		if(start.after(c.getTime())) {
			return start;
		}
		
		if (lastRunTime != null)
			c.setTime(lastRunTime);
		else
			c.setTime(start);

		BasicSchedule dup = (BasicSchedule) getDuplicate();
		if(dup == null) {
			return null;
		}
		return dup.getNext(c);
	}

	@Override
	public List<Date> getRunTimesBetween(Date from, Date to) {
		if (from == null || to == null)
			return null;

		BasicSchedule dup = (BasicSchedule) getDuplicate();
		if(dup == null) {
			return null;
		}

		int limit = 1000;
		int count = 0;
		List<Date> dates = new ArrayList<Date>();		
		while (true) {
			Date curr = dup.getNextRunTimeAfter(from);
			if (curr.after(to) || ++count >= limit)
				break;
			dates.add(curr);
			from = curr;
		}
		return dates;
	}

	@Override
	public long getDelay() {
		Date now = new Date();
		long offset = 0l;
		if (start.after(now)) {
			offset = start.getTime() - now.getTime();
			return offset;
		}
		Date next = getNextRunTimeAfter(now);
		if (end != null && next.after(end)) {
			return -1;
		}
		long diff = next.getTime() - now.getTime();
		return diff;
	}

	/**
	 * The method calculates the next run time for this schedule relative to the
	 * specified date. Specific <code>Schedule</code>(s) should override this
	 * method to make proper calculations. Base implementation is sufficient for
	 * Schedules with have no constraints.
	 * 
	 * @param c1
	 *            Calendar relative to which the next run time is calculated
	 * @return next run time for this schedule
	 */
	protected Date getNext(Calendar c1) {
		if (repeatType == RepeatType.SECOND) {
			c1.add(Calendar.SECOND, step);
			return c1.getTime();
		}
		if (repeatType == RepeatType.MINUTE) {
			c1.add(Calendar.MINUTE, step);
			return c1.getTime();
		}
		if (repeatType == RepeatType.HOUR) {
			c1.add(Calendar.HOUR_OF_DAY, step);
			return c1.getTime();
		}
		if (repeatType == RepeatType.DATE) {
			c1.add(Calendar.DATE, step);
			return c1.getTime();
		}
		if (repeatType == RepeatType.WEEK) {
			c1.add(Calendar.WEEK_OF_YEAR, step);
			return c1.getTime();
		}
		if (repeatType == RepeatType.MONTH) {
			c1.add(Calendar.MONTH, step);
			return c1.getTime();
		}
		if (repeatType == RepeatType.YEAR) {
			c1.add(Calendar.YEAR, step);
			return c1.getTime();
		}
		return null;
	}

	@Override
	public Date getStartTime() {
		return start;
	}

	@Override
	public Date getEndTime() {
		return end;
	}

	@Override
	public String toString() {
		Schedule dup = null;
		try {
			dup = (Schedule) clone();
		}
		catch (CloneNotSupportedException e) {
//			log.error(e);
		}
		
		StringBuilder ret = new StringBuilder();
		ret.append("[");
		ret.append(" Start = " + start);
		if(dup != null) {
			ret.append(", Next run time = " + dup.getNextRunTimeAfter(start));
		}
		ret.append(", End = " + end);
		ret.append("]");
		return ret.toString();
	}

	@Override
	public boolean isRepeating() {
		return repeatType != RepeatType.NONE;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
	}
	
	@Override
	public Constraint getConstraint() {
		return this.constraint;
	}
	
	private Schedule getDuplicate() {
		Schedule dup = null;
		try {
			dup = (Schedule) clone();
		}
		catch (CloneNotSupportedException e) {
//			log.error(e);
		}
		return dup;
	}
}
