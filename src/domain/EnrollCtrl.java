package domain;

import java.util.List;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student student, List<CSE> courses) throws EnrollmentRulesViolationException {
		for (CSE course_sescion : courses) {
			if (student.hasPassed(course_sescion.getCourse()))
					throw new EnrollmentRulesViolationException(String.format("The student has already passed %s",
							course_sescion.getCourse().getName()));
			checkPrerequisites(student, course_sescion);
			checkExamTimeConflict(course_sescion, courses);
			checkDuplicateRequest(course_sescion, courses);
		}
		checkRequestedUnitsLimit(student, courses);
		for (CSE course_section : courses)
			student.takeCourse(course_section.getCourse(), course_section.getSection());
	}

	private void checkPrerequisites(Student student, CSE course_section) throws EnrollmentRulesViolationException
	{
		final List<Course> prereqs = course_section.getCourse().getPrerequisites();
		for (Course pre : prereqs)
			if (!student.hasPassed(pre))
				throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a"
						+ " prerequisite of %s", pre.getName(), course_section.getCourse().getName()));
	}

	private void checkExamTimeConflict(CSE course_section, List<CSE> courses) throws EnrollmentRulesViolationException
	{
		for (CSE cse : courses) {
			if (course_section == cse)
				continue;
			if (course_section.getExamTime().equals(cse.getExamTime()))
				throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam"
						+ " time", course_section, cse));
		}
	}

	private void checkDuplicateRequest(CSE course_section, List<CSE> courses) throws EnrollmentRulesViolationException
	{
		for (CSE cse : courses) {
			if (course_section == cse)
				continue;
			if (course_section.getCourse().equals(cse.getCourse()))
				throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice",
						course_section.getCourse().getName()));
		}
	}

	private void checkRequestedUnitsLimit(Student student, List<CSE> courses) throws EnrollmentRulesViolationException
	{
		final double gpa = student.getGPA();
		final int unitsRequested = courses.stream().mapToInt(c -> c.getCourse().getUnits()).sum();
		if ((gpa < 12 && unitsRequested > 14) ||
				(gpa < 16 && unitsRequested > 16) ||
				(unitsRequested > 20))
			throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match"
					+ " GPA of %f", unitsRequested, gpa));
	}
}
