package org.example.coursetracker;

import javax.sql.DataSource;
import org.example.coursetracker.model.Course;
import org.example.coursetracker.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest @Transactional class CourseTrackerApplicationTests
{

  @Autowired
  private DataSource dataSource;

  @Autowired
  private CourseRepository<?, ?> courseRepository;

  @Test void givenDatasourceAvailableWhenAccessDetailsThenExpectrDetails() throws
      SQLException {
    assertThat(dataSource.getClass().getName()).isEqualTo("com.zaxxer.hikari.HikariDataSource");
    assertThat(dataSource.getConnection().getMetaData().getDatabaseProductName()).isEqualTo("H2");
  }

  @Test void givenCreateCourseWhenLoadTheCourseThenExpectSameCourse() {
    Course course = new Course(null, "Rapid Spring Boot Application Development",
        "Spring", 4,
        "Spring Boot gives all the power of the Spring Framework without all of the complexities");
    Course savedCourse = courseRepository.save(course);
    assertThat(courseRepository.findById(savedCourse.getId()).get()).isEqualTo(course);
  }

  @Test void givenUpdateCourseWhenLoadTheCourseThenExpectUpdatedCourse() {
    Course course = new Course(null, "Rapid Spring Boot Application Development",
        "Spring", 4,
        "Spring Boot gives all the power of the Spring Framework without all of the complexities");
    courseRepository.save(course);
    course.setRating(5);
    Course savedCourse = courseRepository.save(course);
    assertThat(courseRepository.findById(savedCourse.getId()).get().getRating()).isEqualTo(5);
  }

  @Test void givenDataAvailableWhenLoadCoursesByCategoryThenExpectAllCourses() {
    assertThat(courseRepository.findAllByCategory("Spring")).hasSize(5);
    assertThat(courseRepository.findAllByCategory("Unknown")).isEmpty();
  }

  @Test void givenDataAvailableWhenLoadCoursesByCategoryOrderByNameThenExpectSortedCourses() {
    assertThat(courseRepository.findAllByCategoryOrderByName("Spring"))
        .extracting(Course::getName).hasSize(5).isSorted();
  }

  @Test void givenDataAvailableWhenCheckCourseExistsByNameThenExpectCorrectAnswer() {
    assertThat(courseRepository.existsByName("Getting Started with Spring Cloud Kubernetes")).isTrue();
    assertThat(courseRepository.existsByName("Unknown course")).isFalse();
  }

  @Test void givenDataAvailableWhenCountCoursesByCategoryThenExpectCount() {
    assertThat(courseRepository.countByCategory("Spring")).isEqualTo(5);
    assertThat(courseRepository.countByCategory("Unknown")).isZero();
  }

  @Test void givenDataAvailableWhenLoadCoursesByNameOrCategoryThenExpectMatchingCourses() {
    assertThat(courseRepository.findByNameOrCategory(
        "Scalable, Cloud Native Data Applications", "Unknown")).hasSize(1);
    assertThat(courseRepository.findByNameOrCategory("Unknown", "Spring")).hasSize(5);
    assertThat(courseRepository.findByNameOrCategory("Unknown", "Unknown")).isEmpty();
  }

  @Test void givenDataAvailableWhenLoadCoursesByNameStartsWithThenExpectMatchingCourses() {
    assertThat(courseRepository.findByNameStartsWith("Getting Started"))
        .extracting(Course::getName)
        .containsExactlyInAnyOrder("Getting Started with Spring Security DSL",
            "Getting Started with Spring Cloud Kubernetes");
  }

  @Test void givenDataAvailableWhenStreamCoursesByCategoryThenExpectAllCourses() {
    try (Stream<Course> courses = courseRepository.streamAllByCategory("Spring"))
    {
      assertThat(courses.count()).isEqualTo(5);
    }
  }
}
