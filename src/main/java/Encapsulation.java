
public class Encapsulation{
  private String studentname;
  private String qualification;
  public Encapsulation(String name, String education){
    this.studentname =name;
    this.qualification =education;

  }
  public static void main(String[] args) {
    Encapsulation s1=new Encapsulation("akash","B.E");
    Encapsulation s2=new Encapsulation("anjali", "B.E");
    System.out.println(s1.studentname);
    System.out.println(s2.studentname);
  }
  
}
// important concept in encapsulation 
 //it is a process of wrapping the data using access specifiers and validating it by using getter and setter
 // we are using to hide the locators and displays only the action and will limit the access of the driver.
 // eg: wil hide the locators like button and text box but will expose the method like click and sendkeys 
 // to perform the action on the web element.  