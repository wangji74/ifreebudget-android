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
package com.ifreebudget.fm.scheduler.task.constraints;

import com.ifreebudget.fm.scheduler.task.Schedule;
import com.ifreebudget.fm.scheduler.task.Schedule.DayOfWeek;
import com.ifreebudget.fm.scheduler.task.Schedule.ScheduleConstraint;
import com.ifreebudget.fm.scheduler.task.Schedule.WeekOfMonth;

/**
 * Defines constraints so that the task can be scheduled to run on specified
 * week of the month (first, second, third, fourth, last) and the specified day
 * of the week
 * 
 * @author mjrz
 * 
 */
public class MonthConstraintDayBased implements Constraint {

	private static final long serialVersionUID = 1L;
	private WeekOfMonth weekOfMonth;
	private DayOfWeek dayOfWeek;

	/**
	 * Sets up the constraint with the specified week of month and day of week
	 * 
	 * @param weekOfMonth
	 * @param dayOfWeek
	 */
	public MonthConstraintDayBased(WeekOfMonth weekOfMonth, DayOfWeek dayOfWeek) {
		this.weekOfMonth = weekOfMonth;
		this.dayOfWeek = dayOfWeek;
	}

	/**
	 * Returns the week of month defined for this constraint
	 * 
	 * @return WeekOfMonth for this constraint
	 */
	public WeekOfMonth getWeekOfMonth() {
		return weekOfMonth;
	}

	/**
	 * Returns day of week defined for this constraint
	 * 
	 * @return DayOfWeek for this constraint
	 */
	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	@Override
	public ScheduleConstraint getType() {
		return Schedule.ScheduleConstraint.MonthConstraintDayBased;
	}

	@Override
	public String toString() {
		return "[" + weekOfMonth + " " + dayOfWeek + "]";
	}
}
