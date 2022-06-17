package domain;

import java.util.List;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
		for (CSE o : courses) {
			if (s.hasPassed(o.getCourse()))
					throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
			checkPrerequisites(s, o);
			checkExamTimeConflict(o, courses);
			checkDuplicateRequest(o, courses);
		}
		checkRequestedUnitsLimit(s, courses);
		for (CSE o : courses)
			s.takeCourse(o.getCourse(), o.getSection());
	}

	private void checkPrerequisites(Student s, CSE o) throws EnrollmentRulesViolationException
	{
		final List<Course> prereqs = o.getCourse().getPrerequisites();
		for (Course pre : prereqs)
			if (!s.hasPassed(pre))
				throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
	}

	private void checkExamTimeConflict(CSE o, List<CSE> courses) throws EnrollmentRulesViolationException
	{
		for (CSE o2 : courses) {
			if (o == o2)
				continue;
			if (o.getExamTime().equals(o2.getExamTime()))
				throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2));
		}
	}

	private void checkDuplicateRequest(CSE o, List<CSE> courses) throws EnrollmentRulesViolationException
	{
		for (CSE o2 : courses) {
			if (o == o2)
				continue;
			if (o.getCourse().equals(o2.getCourse()))
				throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName()));
		}
	}

	private void checkRequestedUnitsLimit(Student s, List<CSE> courses) throws EnrollmentRulesViolationException
	{
		final double gpa = s.getGPA();
		final int unitsRequested = courses.stream().mapToInt(c -> c.getCourse().getUnits()).sum();
		if ((gpa < 12 && unitsRequested > 14) ||
				(gpa < 16 && unitsRequested > 16) ||
				(unitsRequested > 20))
			throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, gpa));
	}
}
