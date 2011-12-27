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

import java.io.Serializable;

import com.ifreebudget.fm.scheduler.task.Schedule;

/**
 * Constraint can be added to a schedule. A constraint defined on a schedule can
 * be used to further tweak the recurrence behavior. Following types of
 * constraints are available:
 * <p>
 * <code>WeekConstraint</code> when set to a WeekSchedule specifies which days
 * of the week the task will execute
 * <p>
 * <code>MonthConstraint</code> when set to a MonthSchedule specifies which date
 * of the month the task will execute
 * <p>
 * <code>MonthConstraintDayBased</code> when set to a MonthScheduleDayBased
 * specifies which week of the month (first, second, third, fourth, last) and
 * day of the week the task will execute
 * 
 * @see Schedule
 * @author mjrz
 * 
 */
public interface Constraint extends Serializable {
	/**
	 * Returns the type of the constraint. Type is specified by enum
	 * <code>ScheduleConstraint</code>
	 * 
	 * @see ScheduleConstraint
	 * @return type of the constraint for this schedule
	 */
	public Schedule.ScheduleConstraint getType();
}
