/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.ssp.dao.jobqueue;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jasig.ssp.dao.AbstractAuditableCrudDao;
import org.jasig.ssp.dao.AuditableCrudDao;
import org.jasig.ssp.model.Message;
import org.jasig.ssp.model.jobqueue.Job;
import org.jasig.ssp.model.jobqueue.WorkflowStatus;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortDirection;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.springframework.stereotype.Repository;

/**
 * DAO for the {@link Job} model
 */
@Repository
public class JobDao extends AbstractAuditableCrudDao<Job> implements
		AuditableCrudDao<Job> {

	/**
	 * Constructor that initializes the instance with the specific class types
	 * for super class method use.
	 */
	public JobDao() {
		super(Job.class);
	}

	@SuppressWarnings("unchecked")
	public List<Job> getNextQueuedJobsForExecution(int maxResults, String processId) {
		 return sessionFactory
			.getCurrentSession()
			// This really only happens to work b/c we only support single-node deployments.
			.createQuery(
					"from Job where workflowStoppedDate is null" +
						" and ((workflowStatus = :queued)" +
							" or ((workflowStatus = :scheduling or workflowStatus = :executing) and scheduledByProcess != :processId)" +
						")" +
						" order by createdDate")
			.setMaxResults(maxResults).setString("queued", WorkflowStatus.QUEUED.toString()).setString("scheduling", WorkflowStatus.SCHEDULING.toString()).setString("executing", WorkflowStatus.EXECUTING.toString())
			.setString("processId", processId)
			.list();
	}
}