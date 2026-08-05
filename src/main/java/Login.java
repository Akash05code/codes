public class Login extends BaseClass {
    int a = 30;
    int b = 40;
    
    public static void main(String[] args) {
        System.out.println("This is the Login class");
            Login login = new Login();
            login.display();
            
            }
    void display(){
        int c = super.a +super.b;
        System.out.println(c);
        
    }

}
