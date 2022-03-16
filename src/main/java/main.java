public class main {

    public static void main(String[] args)  throws  Exception{
        if(args[0].equals("1")) {
            System.out.println("s1");
            Config.setup1();
        }
        else if(args[0].equals("2")) {
            System.out.println("s2");
            Config.setup2();
        }
        else if(args[0].equals("3")) {
            System.out.println("s3");
            Config.setup3();
        }
        else{
            System.out.println("s4");
            Config.setup4();
        }
        new ChatServer().init();
    }
}
