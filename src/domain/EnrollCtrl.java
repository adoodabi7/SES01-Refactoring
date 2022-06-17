package domain;

import java.util.List;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student student, List<CSE> courses) throws EnrollmentRulesViolationException {
		checkForAlreadyPassedCourses(student, courses);
		checkPrerequisites(student, courses);
		checkExamTimeConflict(courses);
		checkDuplicateRequest(courses);
		checkRequestedUnitsLimit(student, courses);

		for (CSE course_section : courses)
			student.takeCourse(course_section.getCourse(), course_section.getSection());
	}

	private void checkForAlreadyPassedCourses(Student student, List<CSE> courses) throws EnrollmentRulesViolationException
	{
		for (CSE course_section : courses)
			if (student.hasPassed(course_section.getCourse()))
					throw new EnrollmentRulesViolationException(String.format("The student has already passed %s",
							course_section.getCourse().getName()));
	}

	private void checkPrerequisites(Student student, List<CSE> courses) throws EnrollmentRulesViolationException
	{
		for (CSE course_section : courses) {
			final List<Course> prereqs = course_section.getCourse().getPrerequisites();
			for (Course pre : prereqs)
				if (!student.hasPassed(pre))
					throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a"
							+ " prerequisite of %s", pre.getName(), course_section.getCourse().getName()));
		}
	}

	private void checkExamTimeConflict(List<CSE> courses) throws EnrollmentRulesViolationException
	{
		for (CSE cse : courses) {
			for (CSE other : courses) {
				if (cse == other)
					continue;
				if (cse.getExamTime().equals(other.getExamTime()))
					throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam"
							+ " time", cse, other));
			}
		}
	}

	private void checkDuplicateRequest(List<CSE> courses) throws EnrollmentRulesViolationException
	{
		for (CSE cse : courses) {
			for (CSE other : courses) {
				if (cse == other)
					continue;
				if (cse.getCourse().equals(other.getCourse()))
					throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice",
							cse.getCourse().getName()));
			}
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
