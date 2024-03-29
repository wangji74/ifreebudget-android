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
import com.ifreebudget.fm.scheduler.task.constraints.MonthConstraint;

/**
 * Schedule describing monthly recurring tasks constraied with MonthConstraint
 * 
 * @author mjrz
 * @see MonthConstraint
 */
public class MonthSchedule extends BasicSchedule {

	private static final long serialVersionUID = 1L;

	public MonthSchedule(Date start, Date end) throws Exception {
		super(start, end);
	}

	@Override
	protected Date getNext(Calendar c1) {
		if (constraint == null)
			return super.getNext(c1);
		else {
			c1.add(Calendar.MONTH, getStep());

			adjustDate(c1);
			
			return c1.getTime();
		}
	}

	private void adjustDate(Calendar c1) {
		MonthConstraint dc = (MonthConstraint) constraint;

		int inst = dc.getInstance();

		c1.set(Calendar.DAY_OF_MONTH, inst);
		
		Date now = new Date();
		if(c1.getTime().before(now)) {
			c1.add(Calendar.MONTH, getStep());
			adjustDate(c1);
		}

		this.start = c1.getTime();
	}
	
	@Override
	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
		Calendar c = Calendar.getInstance();
		c.setTime(start);
		adjustDate(c);
	}
}
