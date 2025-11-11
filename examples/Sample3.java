class Sample3 {
    public static void main(String[] args) {
        System.out.println(a); // use-before-declaration
        int a = 10;
        int b;
        System.out.println(b); // use-before-initialization
        b = 5;
        System.out.println(b); // OK
    }
}