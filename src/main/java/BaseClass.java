public class BaseClass {
                     int a = 10;
       int b = 20;
      
    public static void main(String[] args) {
         System.out.println("Hello World");
            BaseClass obj = new BaseClass();
            obj.display();
          
    }
    void display(){
        System.out.println("This is a method in BaseClass");
          
    }

}
 // “Inheritance is an OOPS concept where one class acquires the properties and methods of another 
 // class using the extends keyword.

//In Selenium automation frameworks, inheritance is mainly used for code reusability and 
// to avoid code duplication.

//For example, we create a BaseTest class that contains common functionalities such as browser setup, 
// driver initialization, waits, and teardown methods. Then test classes like LoginTest or PaymentTest extend the BaseTest class and reuse those methods instead of rewriting the same code multiple times.

//This improves framework maintainability, readability, and overall structure.”



//Diff between this and super keyword:
//if we use this it will refer to the current class object.(to call method and variable of current class)
//if we use super it will refer to the parent class object.(to call method and variable of parent class)
// in a constructor only one call of constructor has to be used as 1st statement either this() or super() 
//the constructor call has to be the first statement so we cannot use both this() and super() in the same constructor because both of them needs to be the first statement.                          