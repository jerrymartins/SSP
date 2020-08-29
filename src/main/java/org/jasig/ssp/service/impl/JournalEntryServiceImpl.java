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
package org.jasig.ssp.service.impl;

import org.apache.commons.lang.StringUtils;
import org.jasig.ssp.dao.JournalEntryDao;
import org.jasig.ssp.dao.PersonDao;
import org.jasig.ssp.model.JournalEntry;
import org.jasig.ssp.model.JournalEntryDetail;
import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.service.AbstractRestrictedPersonAssocAuditableService;
import org.jasig.ssp.service.JournalEntryService;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonProgramStatusService;
import org.jasig.ssp.transferobject.reports.BaseStudentReportTO;
import org.jasig.ssp.transferobject.reports.EntityCountByCoachSearchForm;
import org.jasig.ssp.transferobject.reports.EntityStudentCountByCoachTO;
import org.jasig.ssp.transferobject.reports.JournalCaseNotesStudentReportTO;
import org.jasig.ssp.transferobject.reports.JournalStepSearchFormTO;
import org.jasig.ssp.transferobject.reports.JournalStepStudentReportTO;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.jasig.ssp.web.api.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class JournalEntryServiceImpl
		extends AbstractRestrictedPersonAssocAuditableService<JournalEntry>
		implements JournalEntryService {

	private final transient JournalEntryDao dao;

	private final transient PersonProgramStatusService personProgramStatusService;

	private final transient PersonDao personDao;

	@Override
	protected JournalEntryDao getDao() {
		return dao;
	}

	@Override
	public JournalEntry create(final JournalEntry obj)
			throws ObjectNotFoundException, ValidationException {
		final JournalEntry journalEntry = getDao().save(obj);
		transition(journalEntry);
		return journalEntry;
	}

	@Override
	public JournalEntry save(final JournalEntry obj)
			throws ObjectNotFoundException, ValidationException {
		final JournalEntry journalEntry = getDao().save(obj);
		transition(journalEntry);
		return journalEntry;
	}

	@Autowired
	public JournalEntryServiceImpl(JournalEntryDao dao, PersonProgramStatusService personProgramStatusService, PersonDao personDao) {
		this.dao = dao;
		this.personProgramStatusService = personProgramStatusService;
		this.personDao = personDao;
	}

	@Async
	void transition(final JournalEntry journalEntry) throws ValidationException, ObjectNotFoundException {
		for (JournalEntryDetail journalEntryDetail : getDetailForTransition(journalEntry.getJournalEntryDetails())) {
			personProgramStatusService.setTransitionForStudent(journalEntry.getPerson());
		}
	}

	private Set<JournalEntryDetail> getDetailForTransition(Set<JournalEntryDetail> details) {
		return details.stream().
				filter(detail -> detail.getJournalStepJournalStepDetail().getJournalStep().isUsedForTransition())
				.collect(Collectors.toSet());
	}
	
	@Override
	public Long getCountForCoach(Person coach, Date createDateFrom, Date createDateTo, List<UUID> studentTypeIds){
		return dao.getJournalCountForCoach(coach, createDateFrom, createDateTo, studentTypeIds);
	}

	@Override
	public Long getStudentCountForCoach(Person coach, Date createDateFrom, Date createDateTo, List<UUID> studentTypeIds) {
		return dao.getStudentJournalCountForCoach(coach, createDateFrom, createDateTo, studentTypeIds);
	}
	
	@Override
	public PagingWrapper<EntityStudentCountByCoachTO> getStudentJournalCountForCoaches(EntityCountByCoachSearchForm form){
		return dao.getStudentJournalCountForCoaches(form);
	}
	
	@Override
	public PagingWrapper<JournalStepStudentReportTO> getJournalStepStudentReportTOsFromCriteria(JournalStepSearchFormTO personSearchForm,  
			SortingAndPaging sAndP){
		return dao.getJournalStepStudentReportTOsFromCriteria(personSearchForm,  
				sAndP);
	}
	
 	@Override
 	public List<JournalCaseNotesStudentReportTO> getJournalCaseNoteStudentReportTOsFromCriteria(JournalStepSearchFormTO personSearchForm, SortingAndPaging sAndP) throws ObjectNotFoundException{
 		 final List<JournalCaseNotesStudentReportTO> personsWithJournalEntries = dao.getJournalCaseNoteStudentReportTOsFromCriteria(personSearchForm, sAndP);
 		 final Map<String, JournalCaseNotesStudentReportTO> map = new HashMap<String, JournalCaseNotesStudentReportTO>();

 		 for(JournalCaseNotesStudentReportTO entry:personsWithJournalEntries){
 			 map.put(entry.getSchoolId(), entry);
 		 }

 		 final SortingAndPaging personSAndP = SortingAndPaging.createForSingleSortAll(ObjectStatus.ACTIVE, "lastName", "DESC") ;
 		 final PagingWrapper<BaseStudentReportTO> persons = personDao.getStudentReportTOs(personSearchForm, personSAndP);
 		
 		 if (persons == null) {
 			 return personsWithJournalEntries;
 		 }

 		 for (BaseStudentReportTO person:persons) {
			 if (!map.containsKey(person.getSchoolId()) && StringUtils.isNotBlank(person.getCoachSchoolId())) {
				 boolean addStudent = true;
				 if (personSearchForm.getJournalSourceIds()!=null) {
					if (getDao().getJournalCountForPersonForJournalSourceIds(person.getId(), personSearchForm.getJournalSourceIds()) == 0) {
						addStudent = false;
					}
				 }
			 	 if (addStudent) {
					 final JournalCaseNotesStudentReportTO entry = new JournalCaseNotesStudentReportTO(person);
					 personsWithJournalEntries.add(entry);
					 map.put(entry.getSchoolId(), entry);
				 }
 			}
 		 }
		 sortByStudentName(personsWithJournalEntries);

 		 return personsWithJournalEntries;
 	}
 		 
	private static void sortByStudentName(List<JournalCaseNotesStudentReportTO> toSort) {
		Comparator<JournalCaseNotesStudentReportTO> byFirstName =
				(JournalCaseNotesStudentReportTO o1, JournalCaseNotesStudentReportTO o2)->o1.getFirstName().compareToIgnoreCase(o2.getFirstName());

		Comparator<JournalCaseNotesStudentReportTO> byMiddleName =
				(JournalCaseNotesStudentReportTO o1, JournalCaseNotesStudentReportTO o2)->o1.getMiddleName().compareToIgnoreCase(o2.getMiddleName());

		Comparator<JournalCaseNotesStudentReportTO> byLastName =
				(JournalCaseNotesStudentReportTO o1, JournalCaseNotesStudentReportTO o2)->o1.getLastName().compareToIgnoreCase(o2.getLastName());

		toSort.sort(Comparator.nullsFirst(byFirstName).thenComparing(byMiddleName).thenComparing(byLastName));

	}

}