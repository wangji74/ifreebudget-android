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
import java.util.List;
import com.ifreebudget.fm.scheduler.task.constraints.*;

/**
 * Schedule describes the future times when a task will execute.
 * 
 * @author mjrz
 */
public interface Schedule extends Cloneable {
	/**
	 * Sets the start time of the schedule.
	 * 
	 * @param date
	 *            start time of the schedule
	 */
	public void setStartTime(Date date);

	/**
	 * Gets the start time of the schedule
	 * 
	 * @return the start time of the schedule.
	 */
	public Date getStartTime();

	/**
	 * Gets the end time of the schedule.
	 * 
	 * @return time when the schedule ends.
	 */
	public Date getEndTime();

	/**
	 * Gets the next run time of the task which has this schedule relative to
	 * the last run time.
	 * 
	 * @return next run time.
	 */
	public Date getNextRunTime();

	/**
	 * Last run time of the task which has this schedule.
	 * 
	 * @return returns when the schedule was run last time.
	 */
	public Date getLastRunTime();

	/**
	 * Sets last run time for that task that has this schedule.
	 * @param lastRunTime last time when the task was executed.
	 */
	public void setLastRunTime(Date lastRunTime);

	/**
	 * Returns date when the task which has this schedule will execute relative
	 * to the specified date
	 * 
	 * @param date
	 *            Date from which to calculate next run time
	 * @return Date
	 */
	public Date getNextRunTimeAfter(Date date);

	/**
	 * Returns list of Date on which the task which has this schedule will
	 * execute between the specified dates
	 * 
	 * @param from
	 *            starting Date from which to calculate next run times.
	 * @param to
	 *            ending Date upto which the run times will be calculated.
	 * @return list containing execution dates.
	 */
	public List<Date> getRunTimesBetween(Date from, Date to);

	/**
	 * Sets the recurrence type for this schedule. <br>
	 * Example: RepeatType of MONTH with step of 2 will execute every second
	 * month.
	 * 
	 * @param type
	 *            RepeatType for the type of recurrence
	 * @param step
	 *            for the number of intervals between recurrence.
	 * @see RepeatType
	 */
	public void setRepeatType(RepeatType type, int step);

	/**
	 * Gets the repeat type for this schedule.
	 * @return RepeatType
	 */
	public RepeatType getRepeatType();

	/**
	 * Sets the constraints for this schedule. Constraints define the dates on
	 * which a task will be executed. For example, WeekConstraint can be setup
	 * so that the task executes on specified days of the week
	 * 
	 * @param constraint
	 */
	public void setConstraint(Constraint constraint);

	/**
	 * Gets the constraint defined for this schedule
	 * 
	 * @return constraint for this schedule
	 */
	public Constraint getConstraint();

	/**
	 * Returns the increment to be used in calculating the next run date. For
	 * example, a daily schedule with step of 2 indicates the schedule will be
	 * fired every two days
	 * 
	 * @return increment size for the specified recurring schedule
	 */
	public int getStep();

	/**
	 * Returns the number of milliseconds until next fire time
	 * 
	 * @return number of milliseconds until next fire time
	 */
	public long getDelay();

	/**
	 * To check if the task is repeating
	 * 
	 * @return <tt>true</tt> if the task is repeating, <tt>false</tt> otherwise
	 */
	public boolean isRepeating();

	public static enum RepeatType {
		NONE(0), SECOND(1), MINUTE(2), HOUR(3), DATE(4), WEEK(5), MONTH(6), YEAR(
				7), DAYOFWEEK(8), DAYOFMONTH(9);

		private int index;

		private RepeatType(int index) {
			this.index = index;
		}

		public int getType() {
			return index;
		}
	}

	public static enum DayOfWeek {
		Sunday(Calendar.SUNDAY), Monday(Calendar.MONDAY), Tuesday(
				Calendar.TUESDAY), Wednesday(Calendar.WEDNESDAY), Thursday(
				Calendar.THURSDAY), Friday(Calendar.FRIDAY), Saturday(
				Calendar.SATURDAY);

		private int index;

		private DayOfWeek(int index) {
			this.index = index;
		}

		public int getDayOfWeek() {
			return index;
		}
	}

	public static enum WeekOfMonth {
		First(1), Second(2), Third(3), Fourth(4), Last(5);

		private int index;

		private WeekOfMonth(int index) {
			this.index = index;
		}

		public int getWeekOfMonth() {
			return index;
		}
	}

	/**
	 * Enum for types of constraints
	 * 
	 * @author mjrz
	 * 
	 */
	public static enum ScheduleConstraint {
		DayConstraint(1), WeekConstraint(2), MonthConstraint(3), MonthConstraintDayBased(
				4);

		private int index;

		private ScheduleConstraint(int index) {
			this.index = index;
		}

		public int getScheduleConstraint() {
			return index;
		}
	}
}
