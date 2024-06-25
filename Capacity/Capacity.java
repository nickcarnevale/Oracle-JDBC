// Nicholas Carnevale
// nic225@lehigh.edu  
// CSE241  
// Homework 4  
// 10.02.2023  

import java.sql.*;
import java.util.*;

public class Capacity {

    //Lehigh University Database URL
    static final String DB_URL = "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241";

    public static void main(String[] args){
        
        //initialize connection and scanner
        Connection conn = null;
        Scanner scanner = new Scanner(System.in);
        
        do {
            try { 
                //get username and password
                System.out.print("\nEnter Oracle user id: ");
                String user = scanner.nextLine();
                System.out.print("Enter Oracle password: ");
                String pass = scanner.nextLine();

                //initalize the connection to the database
                conn = DriverManager.getConnection(DB_URL, user, pass);
                System.out.println("You have connected successfully.\n");

                //assignment specific code
                
                //initialize variables
                String semester = "";
                int course_id = 0; 
                int sec_id = 0; 
                int year = 0;
                int capacity = 0;
                int totalEnrollment = 0;
                System.out.println("Input data on the section whose classroom capacity you would like to check.\n");
                boolean valid = true;
                List<String> secIds = new ArrayList<>(); 

                //year
                do {
                    System.out.print("Year(yyyy) or 0 to exit: ");
                    if(scanner.hasNextInt()){
                        year = scanner.nextInt();
                        scanner.nextLine();
                        if(year > 2000 && year < 2011){
                            
                            valid = true;
                            //success

                        } else if(year == 0){
                            //close the connection
                            conn.close();
                            //terminate the program
                            System.exit(0);

                        } else {
                            valid = false;
                            System.out.println("Year not in database.");
                        }

                    } else {
                        valid = false;
                        System.out.println("Please input an integer.");
                        scanner.nextLine();
                    } 
                } while (!valid);

                //semester
                do {
                    System.out.print("Semester(fall, winter, sprint, summer): ");  
                    semester = scanner.nextLine();
                    if(semester.equalsIgnoreCase("fall")){     
                        
                        semester = "Fall";
                        valid = true;

                    } else if(semester.equalsIgnoreCase("spring")){

                        semester = "Spring";
                        valid = true;
                        
                    } else if(semester.equalsIgnoreCase("summer")){

                        valid = false;
                        System.out.println("There were no classes in Summer of " + year + ". Please enter another semester.");
            
                    } else if(semester.equalsIgnoreCase("winter")){
                        
                        valid = false;
                        System.out.println("There were no classes in Winter of " + year + ". Please enter another semester.");

                    } else {
                        valid = false;
                        System.out.println("Enter a valid semester.");
                    }
                } while (!valid);

                //course_id
                do {
                    System.out.print("Input course ID as 3 digit integer: ");
                    if(scanner.hasNextInt()){
                        course_id = scanner.nextInt();
                        scanner.nextLine();
                        
                        //print the sections of that course, if it is offered
                        String query = "SELECT sec_id FROM section WHERE course_id = ? AND year = ? AND semester = ? ";
                        PreparedStatement sections = conn.prepareStatement(query);
                        sections.setInt(1, course_id);
                        sections.setInt(2, year); 
                        sections.setString(3, semester);
                        ResultSet sectionsResult = sections.executeQuery();
                        while (sectionsResult.next()) {
                            String secId = sectionsResult.getString("sec_id");
                            secIds.add(secId);
                        }
                        if(!secIds.isEmpty()){
                            valid = true;
                            System.out.println("These are the avaliable sections for course " + course_id + ": " + secIds);
                        }else{
                            valid = false;
                            System.out.println("This course is not offered in this term.");
                        }
                        sections.close();
                        sectionsResult.close();
                    } else {
                        valid = false;
                        System.out.println("Please input a 3 digit integer.");
                        scanner.nextLine();
                    } 
                } while (!valid);

                //year
                do {
                    System.out.print("Input section ID as an integer: ");
                    if(scanner.hasNextInt()){
                        sec_id = scanner.nextInt();
                        scanner.nextLine();
                        String check = String.valueOf(sec_id);
                        if(secIds.contains(check)){
                            valid = true;
                        } else {
                            valid = false;
                            System.out.println("That section ID is not avaliable.");
                        }
                    } else {
                        valid = false;
                        System.out.println("Please input an integer.");
                        scanner.nextLine();
                    } 
                } while (!valid);
  

                System.out.println("The selected course is: ");
                System.out.println("Course ID: " + course_id);
                System.out.println("Section: " + sec_id);
                System.out.println("Term: " + semester + ", " + year);

                //compute capacity
                
                String query2 = "select s.course_id, s.sec_id, s.semester, s.year, c.capacity, count(t.ID) as total_enrollment " + 
                                "FROM section s JOIN classroom c on s.room_number = c.room_number and s.building = c.building " + 
                                "left join takes t on s.course_id = t.course_id and s.sec_id = t.sec_id and s.semester = t.semester and s.year = t.year " +  
                                "where s.course_id = ? AND s.sec_id = ? AND s.semester = ? AND s.year = ? " + 
                                "group by s.course_id, s.sec_id, s.semester, s.year, c.capacity "; 
                
                PreparedStatement find = conn.prepareStatement(query2);
                find.setInt(1, course_id);
                find.setInt(2, sec_id);
                find.setString(3, semester);
                find.setInt(4, year);

                ResultSet out = find.executeQuery();

                while(out.next()){
                    capacity = out.getInt("capacity");
                    totalEnrollment = out.getInt("total_enrollment");
                }

                System.out.print("Capacity is " + capacity + ". ");
                System.out.println("Total Enrollment is " + totalEnrollment + ".");

                if(capacity > totalEnrollment){
                    System.out.println("There are " + (capacity - totalEnrollment) + " open seats.\n");
                } else if(capacity == totalEnrollment){
                    System.out.println("The class is at max capacity.");
                }
                else{
                    System.out.println("The class is overenrolled by " + (totalEnrollment - capacity) + " seats.\n");
                }

                find.close();
                out.close();

                //close the connection
                conn.close();
                
                //terminate the program
                System.exit(0);
            } catch (SQLException e){
                e.printStackTrace();
                System.out.println("[Error]: Connection Error. Re-enter login data.");
            }
        } while(conn == null);
     
    }
}