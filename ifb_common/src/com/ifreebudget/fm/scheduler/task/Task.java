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

/**
 * A task that can be scheduled using the scheduler.
 */
public interface Task extends Cloneable {
	/**
	 * Name of this task
	 * @return name of this task
	 */
	public String getName();
	
	/**
	 * Set the schedule for this task. Schedule defines the recurrance pattern and start, end dates for this task 
	 * @param schedule Schedule to set
	 * @see Schedule
	 */
	public void setSchedule(Schedule schedule);
	
	/**
	 * Gets the schedule defined for this task
	 * 
	 * @return schedule for this task
	 */
	public Schedule getSchedule();
}
