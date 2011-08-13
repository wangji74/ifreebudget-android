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

/**
 * DayConstraint constraints the actual day of the week when a task will be
 * executed
 * 
 * @author mjrz
 * 
 */
public class DayConstraint implements Constraint {
	private static final long serialVersionUID = 1L;
	private DayOfWeek dayOfWeek;
	
	public DayConstraint(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	
	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}
	@Override
	public ScheduleConstraint getType() {
		return Schedule.ScheduleConstraint.DayConstraint;
	}
}
