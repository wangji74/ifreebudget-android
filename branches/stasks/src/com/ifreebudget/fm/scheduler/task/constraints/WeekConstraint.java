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

import java.util.*;

import com.ifreebudget.fm.scheduler.task.Schedule;
import com.ifreebudget.fm.scheduler.task.Schedule.*;

/**
 * Constraints the days of the week when a task will be executed. Multiple days
 * can be added. For example, WeekConstraint can be specified so that the task
 * will execute on Monday, Friday of every week
 * 
 * @author mjrz
 * 
 */
public class WeekConstraint implements Constraint {
	private static final long serialVersionUID = 1L;

	/**
	 * Ordered set of DayOfWeek(s) added for this constraint
	 */
	private TreeSet<DayOfWeek> days;

	/**
	 * Default constructor
	 */
	public WeekConstraint() {
		days = new TreeSet<DayOfWeek>();
	}

	/**
	 * Add a DayOfWeek to this constraint
	 * 
	 * @param dow
	 *            DayOfWeek to add
	 */
	public void addDay(DayOfWeek dow) {
		days.add(dow);
	}

	/**
	 * Returns the DayOfWeek(s) added to this constraint
	 * 
	 * @return set of DayOfWeek for this constraint
	 */
	public Set<DayOfWeek> getDays() {
		return days;
	}

	/**
	 * Gets the first day of the week for this constraint
	 * 
	 * @return first DayOfWeek
	 */
	public DayOfWeek getFirst() {
		return days.first();
	}

	/**
	 * Returns the last day of the week for this constraint
	 * 
	 * @return last DayOfWeek
	 */
	public DayOfWeek getLast() {
		return days.last();
	}

	@Override
	public ScheduleConstraint getType() {
		return Schedule.ScheduleConstraint.WeekConstraint;
	}
}
