package HW2;

import HW2.business.*;
import HW2.data.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import HW2.data.PostgreSQLErrorCodes;
//import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;

import static HW2.business.ReturnValue.*;

public class Solution {
    public static void createTables() {
        InitialState.createInitialState();
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("CREATE TABLE Test (TestID INTEGER NOT NULL,\n" +
                                                    "Semester INTEGER NOT NULL,\n" +
                                                    "Time INTEGER NOT NULL,\n" +
                                                    "Room INTEGER NOT NULL,\n" +
                                                    "Day INTEGER NOT NULL,\n" +
                                                    "CreditPoints INTEGER NOT NULL,\n" +
                                                    "PRIMARY KEY(TestID, Semester),\n" +
                                                    "CHECK(TestID>0 AND Room>0 AND CreditPoints>0 " +
                                                    "AND Semester BETWEEN 1 AND 3 AND Time BETWEEN 1 AND 3 AND " +
                                                    "DAY BETWEEN 1 AND 31))");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE TABLE Student (StudentID INTEGER NOT NULL,\n" +
                                                    "Name TEXT NOT NULL,\n" +
                                                    "Faculty TEXT NOT NULL,\n" +
                                                    "CreditPoints INTEGER NOT NULL,\n" +
                                                    "PRIMARY KEY(StudentID),\n" +
                                                    "CHECK(StudentID>0 AND CreditPoints>=0))");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE TABLE Supervisor (SupervisorID INTEGER NOT NULL,\n" +
                                                    "Name TEXT NOT NULL,\n" +
                                                    "Salary INTEGER NOT NULL,\n" +
                                                    "PRIMARY KEY(SupervisorID),\n" +
                                                    "CHECK(SupervisorID>0 AND Salary>=0))");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE TABLE Attend (StudentID INTEGER NOT NULL,\n" +
                                                    "TestID INTEGER NOT NULL,\n" +
                                                    "Semester INTEGER NOT NULL,\n" +
                                                    "FOREIGN KEY(StudentID) REFERENCES Student(StudentID),\n" +
                                                    "FOREIGN KEY(TestID, Semester) REFERENCES Test(TestID, Semester), \n" +
                                                    "PRIMARY KEY(StudentID,TestID,Semester))");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE TABLE Oversee (SupervisorID INTEGER NOT NULL,\n" +
                                                    "TestID INTEGER NOT NULL,\n" +
                                                    "Semester INTEGER NOT NULL,\n" +
                                                    "FOREIGN KEY(SupervisorID) REFERENCES Supervisor(SupervisorID),\n" +
                                                    "FOREIGN KEY(TestID, Semester) REFERENCES Test(TestID, Semester), \n" +
                                                    "PRIMARY KEY(SupervisorID,TestID,Semester))");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE VIEW CpAfterAttend AS " +
                                                    "SELECT Student.StudentID, Student.Faculty, " +
                                                    "COALESCE(SUM(Test.CreditPoints),0) + Student.CreditPoints as CP " +
                                                    "FROM Student LEFT OUTER JOIN Attend NATURAL JOIN Test ON Student.StudentID = Attend.StudentID " +
                                                    "GROUP BY Student.StudentID");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE VIEW Salaries AS " +
                                                    "SELECT Supervisor.SupervisorID, Salary, COUNT(TestID), Salary*COUNT(TestID) as Wage\n" +
                                                    "FROM Supervisor LEFT OUTER JOIN Oversee ON Supervisor.SupervisorID = Oversee.SupervisorID\n" +
                                                    "GROUP BY Supervisor.SupervisorID");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE VIEW StudentWithALLStudents AS\n" +
                                                    "SELECT stu1.StudentID as StudentID1, stu2.StudentID as StudentID2\n" +
                                                    "FROM Student stu1, Student stu2\n" +
                                                    "where stu1.StudentID <> stu2.StudentID\n" +
                                                    "Order BY stu1.StudentID ASC");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE VIEW StudentTestCount AS\n" +
                                                    "SELECT Student.StudentID AS StudentID1, COUNT(Attend.StudentID)\n" +
                                                    "FROM Student LEFT OUTER JOIN Attend ON Student.StudentID = Attend.StudentID\n" +
                                                    "GROUP BY Student.StudentID\n" +
                                                    "ORDER BY Student.StudentID ASC");
            pstmt.execute();
            pstmt = connection.prepareStatement("CREATE VIEW Same AS\n" +
                                                    "SELECT attend1.StudentID as StudentID1, attend2.StudentID as StudentID2, COUNT(attend1.TestID) As cnt_same\n" +
                                                    "FROM Attend attend1, Attend attend2\n" +
                                                    "where attend1.StudentID < attend2.StudentID AND attend1.TestID = attend2.TestID AND attend1.Semester = attend2.Semester\n" +
                                                    "GROUP BY attend1.StudentID, attend2.StudentID");
            pstmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void clearTables() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("TRUNCATE TABLE Attend, Oversee, Student, Supervisor, Test");
            pstmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void dropTables() {
        InitialState.dropInitialState();
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DROP VIEW IF EXISTS Salaries, CpAfterAttend, StudentWithALLStudents, StudentTestCount, Same");
            pstmt.execute();
            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS Attend, Oversee, Student, Supervisor, Test");
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static ReturnValue addTest(Test test) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;

        try {
            pstmt = connection.prepareStatement("INSERT INTO Test  VALUES (?, ?, ?, ?, ?, ?)");
            pstmt.setInt(1,test.getId());
            pstmt.setInt(2,test.getSemester());
            pstmt.setInt(3,test.getTime());
            pstmt.setInt(4,test.getRoom());
            pstmt.setInt(5,test.getDay());
            pstmt.setInt(6,test.getCreditPoints());
            pstmt.execute();

        } catch (SQLException e) {
            //e.printStackTrace()();
            if(Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue())
            {
                return ALREADY_EXISTS;
            }
            else if (Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue())
            {
                return BAD_PARAMS;
            }
            else if (Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.NOT_NULL_VIOLATION.getValue())
            {
                return BAD_PARAMS;
            }
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return OK;
    }

    public static Test getTestProfile(Integer testID, Integer semester) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        Test testToReturn = new Test();
        try {
            pstmt = connection.prepareStatement("SELECT * FROM Test where TestID = ? AND Semester = ?");
            pstmt.setInt(1,testID);
            pstmt.setInt(2,semester);
            ResultSet results = pstmt.executeQuery();
            if (!results.next()){
                results.close();
                return Test.badTest();
            }
            testToReturn.setId(results.getInt("TestID"));
            testToReturn.setSemester(results.getInt("Semester"));
            testToReturn.setTime(results.getInt("Time"));
            testToReturn.setRoom(results.getInt("Room"));
            testToReturn.setDay(results.getInt("Day"));
            testToReturn.setCreditPoints(results.getInt("CreditPoints"));
            results.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return testToReturn;
    }

    public static ReturnValue deleteTest(Integer testID, Integer semester) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;

        try {
            pstmt = connection.prepareStatement("DELETE FROM Test where TestID = ? AND Semester = ?");
            pstmt.setInt(1,testID);
            pstmt.setInt(2,semester);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0){
                return NOT_EXISTS;
            }

        } catch (SQLException e) {
            //e.printStackTrace()();
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return OK;
    }

    public static ReturnValue addStudent(Student student) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;

        try {
            pstmt = connection.prepareStatement("INSERT INTO Student VALUES (?, ?, ?, ?)");
            pstmt.setInt(1,student.getId());
            pstmt.setString(2,student.getName());
            pstmt.setString(3,student.getFaculty());
            pstmt.setInt(4,student.getCreditPoints());
            pstmt.execute();

        } catch (SQLException e) {
            //e.printStackTrace()();
            if(Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue())
            {
                return ALREADY_EXISTS;
            }
            else if (Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue())
            {
                return BAD_PARAMS;
            }
            else if (Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.NOT_NULL_VIOLATION.getValue())
            {
                return BAD_PARAMS;
            }
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return OK;
    }

    public static Student getStudentProfile(Integer studentID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        Student studentToReturn = new Student();
        try {
            pstmt = connection.prepareStatement("SELECT * FROM Student where StudentID = ?");
            pstmt.setInt(1,studentID);
            ResultSet results = pstmt.executeQuery();
            if (!results.next()){
                results.close();
                return Student.badStudent();
            }
            studentToReturn.setId(results.getInt("StudentID"));
            studentToReturn.setName(results.getString("Name"));
            studentToReturn.setFaculty(results.getString("Faculty"));
            studentToReturn.setCreditPoints(results.getInt("CreditPoints"));
            results.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return studentToReturn;
    }

    public static ReturnValue deleteStudent(Integer studentID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DELETE FROM Student where StudentID = ?");
            pstmt.setInt(1,studentID);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0){
                return NOT_EXISTS;
            }
        } catch (SQLException e) {
            //e.printStackTrace()();
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return OK;
    }

    public static ReturnValue addSupervisor(Supervisor supervisor) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;

        try {
            pstmt = connection.prepareStatement("INSERT INTO Supervisor VALUES (?, ?, ?)");
            pstmt.setInt(1,supervisor.getId());
            pstmt.setString(2,supervisor.getName());
            pstmt.setInt(3,supervisor.getSalary());
            pstmt.execute();

        } catch (SQLException e) {
            //e.printStackTrace()();
            if(Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue())
            {
                return ALREADY_EXISTS;
            }
            else if (Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue())
            {
                return BAD_PARAMS;
            }
            else if (Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.NOT_NULL_VIOLATION.getValue())
            {
                return BAD_PARAMS;
            }
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return OK;
    }

    public static Supervisor getSupervisorProfile(Integer supervisorID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        Supervisor supervisorToReturn = new Supervisor();
        try {
            pstmt = connection.prepareStatement("SELECT * FROM Supervisor where SupervisorID = ?");
            pstmt.setInt(1,supervisorID);
            ResultSet results = pstmt.executeQuery();
            if (!results.next()){
                results.close();
                return Supervisor.badSupervisor();
            }
            supervisorToReturn.setId(results.getInt("SupervisorID"));
            supervisorToReturn.setName(results.getString("Name"));
            supervisorToReturn.setSalary(results.getInt("Salary"));
            results.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return supervisorToReturn;

    }

    public static ReturnValue deleteSupervisor(Integer supervisorID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DELETE FROM Supervisor where SupervisorID = ?");
            pstmt.setInt(1,supervisorID);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0){
                return NOT_EXISTS;
            }
        } catch (SQLException e) {
            //e.printStackTrace()();
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return OK;
    }

    public static ReturnValue studentAttendTest(Integer studentID, Integer testID, Integer semester) {

        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;

        try {
            pstmt = connection.prepareStatement("INSERT INTO Attend VALUES (?, ?, ?)");
            pstmt.setInt(1,studentID);
            pstmt.setInt(2,testID);
            pstmt.setInt(3,semester);
            pstmt.execute();

        } catch (SQLException e) {
            //e.printStackTrace()();
            if(Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.FOREIGN_KEY_VIOLATION.getValue())
            {
                return NOT_EXISTS;
            }
            if(Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue()) {
                return ALREADY_EXISTS;
            }
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return OK;
    }

    public static ReturnValue studentWaiveTest(Integer studentID, Integer testID, Integer semester) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DELETE FROM Attend where StudentID = ? AND TestID = ? AND Semester = ?");
            pstmt.setInt(1, studentID);
            pstmt.setInt(2, testID);
            pstmt.setInt(3, semester);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0){
                return NOT_EXISTS;
            }
        } catch (SQLException e) {
            //e.printStackTrace()();
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return OK;
    }

    public static ReturnValue supervisorOverseeTest(Integer supervisorID, Integer testID, Integer semester) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;

        try {
            pstmt = connection.prepareStatement("INSERT INTO Oversee  VALUES (?, ?, ?)");
            pstmt.setInt(1,supervisorID);
            pstmt.setInt(2,testID);
            pstmt.setInt(3,semester);
            pstmt.execute();

        } catch (SQLException e) {
            //e.printStackTrace()();
            if(Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.FOREIGN_KEY_VIOLATION.getValue())
            {
                return NOT_EXISTS;
            }
            if(Integer.parseInt(e.getSQLState()) == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue()) {
                return ALREADY_EXISTS;
            }
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return OK;
    }

    public static ReturnValue supervisorStopsOverseeTest(Integer supervisorID, Integer testID, Integer semester) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DELETE FROM Oversee where SupervisorID = ? AND TestID = ? AND Semester = ?");
            pstmt.setInt(1, supervisorID);
            pstmt.setInt(2, testID);
            pstmt.setInt(3, semester);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0){
                return NOT_EXISTS;
            }
        } catch (SQLException e) {
            //e.printStackTrace()();
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return OK;
    }

    public static Float averageTestCost() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        float average = 0f;
        try {
            pstmt = connection.prepareStatement("SELECT SUM(per_test_average)/(SELECT COUNT(*) FROM Test) as Average " +
                                                    "FROM (SELECT AVG(Salary) AS per_test_average " +
                                                          "FROM Supervisor NATURAL JOIN Oversee " +
                                                          "GROUP BY TestID,Semester) AS per_supervisor_average");
            ResultSet results = pstmt.executeQuery();
            if (!results.next()) {
                results.close();
                return -1f;
            }
            average = results.getFloat("Average");
            results.close();
        } catch (SQLException e) {
            //e.printStackTrace()();
            return 0f;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return average;
    }

    public static Integer getWage(Integer supervisorID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        int wage = -1;
        try {
            pstmt = connection.prepareStatement("SELECT Wage " +
                                                    "FROM Salaries " +
                                                    "where SupervisorID = ?");
            pstmt.setInt(1, supervisorID);
            ResultSet results = pstmt.executeQuery();
            while(results.next()) {

                wage = results.getInt("Wage");
            }
            results.close();
        } catch (SQLException e) {
            return -1;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return wage;
    }

    public static ArrayList<Integer> supervisorOverseeStudent() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ArrayList<Integer> students = new ArrayList<Integer>();
        try {
            pstmt = connection.prepareStatement("SELECT DISTINCT StudentID " +
                                                    "FROM Attend NATURAL JOIN Oversee " +
                                                    "GROUP BY StudentID,SupervisorID " +
                                                    "HAVING COUNT(*)>1 " +
                                                    "ORDER BY StudentID DESC");
            ResultSet results = pstmt.executeQuery();
            while (results.next()) {
                students.add(results.getInt("StudentID"));
            }
            results.close();
        } catch (SQLException e) {
            //e.printStackTrace()();
            return students;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return students;
    }

    public static ArrayList<Integer> testsThisSemester(Integer semester) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ArrayList<Integer> testIDs = new ArrayList<Integer>();
        try {
            pstmt = connection.prepareStatement("SELECT TestID " +
                                                    "FROM Test " +
                                                    "where Semester = ? " +
                                                    "ORDER BY TestID DESC " +
                                                    "LIMIT 5");
            pstmt.setInt(1, semester);
            ResultSet results = pstmt.executeQuery();
            while (results.next()) {
                testIDs.add(results.getInt("TestID"));
            }
            results.close();
        } catch (SQLException e) {
            //e.printStackTrace()();
            return testIDs;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return testIDs;
    }

    public static Boolean studentHalfWayThere(Integer studentID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT StudentID " +
                                                    "FROM Student NATURAL JOIN CreditPoints " +
                                                    "where StudentID = ? AND CreditPoints >= Points/2 + Points%2");
            pstmt.setInt(1, studentID);
            ResultSet results = pstmt.executeQuery();
            if (!results.next()) {
                results.close();
                return false;
            }
            results.close();
        } catch (SQLException e) {
            //e.printStackTrace()();
            return false;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return true;
    }

    public static Integer studentCreditPoints(Integer studentID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        int creditPoints = 0;
        try {
            pstmt = connection.prepareStatement("SELECT CP " +
                                                    "FROM CpAfterAttend " +
                                                    "where StudentID = ?");
            pstmt.setInt(1, studentID);
            ResultSet results = pstmt.executeQuery();
            while (results.next()) {
                creditPoints = results.getInt("CP");
            }
            results.close();
        } catch (SQLException e) {
            //e.printStackTrace()();
            return -1;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return creditPoints;
    }

    public static Integer getMostPopularTest(String faculty) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        int testID = 0;
        try {
            pstmt = connection.prepareStatement("SELECT TestID, COUNT(TestID) AS Count " +
                                                    "FROM Student NATURAL JOIN Attend " +
                                                    "WHERE Faculty = ? " +
                                                    "GROUP BY TestID " +
                                                    "ORDER BY Count DESC, TestID DESC " +
                                                    "LIMIT 1");
            pstmt.setString(1, faculty);
            ResultSet results = pstmt.executeQuery();
            if (!results.next()) {
                results.close();
                return 0;
            }
            testID = results.getInt("TestID");
            results.close();
        } catch (SQLException e) {
            //e.printStackTrace()();
            return 0;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return testID;
    }

    public static ArrayList<Integer> getConflictingTests() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ArrayList<Integer> testIDs = new ArrayList<Integer>();
        try {
            pstmt = connection.prepareStatement("SELECT DISTINCT test1.TestID " +
                                                    "FROM Test test1, Test test2 " +
                                                    "where test1.TestID <> test2.TestID " +
                                                    "AND test1.Semester = test2.Semester " +
                                                    "AND test1.Day = test2.Day " +
                                                    "AND test1.Time = test2.Time " +
                                                    "ORDER BY test1.TestID ASC");
            ResultSet results = pstmt.executeQuery();
            while (results.next()) {
                testIDs.add(results.getInt("TestID"));
            }
            results.close();
        } catch (SQLException e) {
            //e.printStackTrace()();
            return testIDs;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return testIDs;
    }

    public static ArrayList<Integer> graduateStudents() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ArrayList<Integer> StudentIDs = new ArrayList<Integer>();
        try {
            pstmt = connection.prepareStatement("SELECT StudentID " +
                                                    "FROM CpAfterAttend NATURAL JOIN CreditPoints " +
                                                    "where cp >= points " +
                                                    "ORDER BY StudentID ASC " +
                                                    "LIMIT 5");
            ResultSet results = pstmt.executeQuery();
            while (results.next()) {
                StudentIDs.add(results.getInt("StudentID"));
            }
            results.close();
        } catch (SQLException e) {
            //e.printStackTrace()();
            return StudentIDs;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return StudentIDs;
    }

    public static ArrayList<Integer> getCloseStudents(Integer studentID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        ArrayList<Integer> StudentIDs = new ArrayList<Integer>();
        try {
            pstmt = connection.prepareStatement("SELECT StudentWithALLStudents.StudentID2 AS StudentID\n" +
                                                    "FROM (StudentWithALLStudents NATURAL JOIN StudentTestCount) LEFT OUTER JOIN SAME\n" +
                                                    "ON StudentWithALLStudents.StudentID1 = Same.StudentID1 AND StudentWithALLStudents.StudentID2 = Same.StudentID2\n" +
                                                    "where count<=2*COALESCE(cnt_same,0) AND StudentWithALLStudents.StudentID1 = 1\n" +
                                                    "ORDER BY StudentWithALLStudents.StudentID2 DESC\n" +
                                                    "LIMIT 10");
            ResultSet results = pstmt.executeQuery();
            while (results.next()) {
                StudentIDs.add(results.getInt("StudentID"));
            }
            results.close();
        } catch (SQLException e) {
            //e.printStackTrace()();
            return StudentIDs;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
        return StudentIDs;
    }
}