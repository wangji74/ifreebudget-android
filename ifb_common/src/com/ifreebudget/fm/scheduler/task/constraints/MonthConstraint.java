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
import com.ifreebudget.fm.scheduler.task.Schedule.ScheduleConstraint;

/**
 * Defines constraint so that the task will be executed on the specified date of
 * the month. For example, constraint can be setup to execute task on the 15th
 * of every month.
 * <p>
 * <b>Note: if the constraint is setup so that the date of month exceeds the
 * number of days for that month, it will roll over to the next month.
 * 
 * @author mjrz
 * 
 */
public class MonthConstraint implements Constraint {
	private static final long serialVersionUID = 1L;
	private int instance;

	/**
	 * Sets up the constraint with the specified date of the month
	 * 
	 * @param instance
	 *            specifes the date of the month
	 */
	public MonthConstraint(int instance) {
		this.instance = instance;
	}

	/**
	 * Returns the specified date of the month for this constraint
	 * 
	 * @return
	 */
	public int getInstance() {
		return instance;
	}

	@Override
	public ScheduleConstraint getType() {
		return Schedule.ScheduleConstraint.MonthConstraint;
	}
}
