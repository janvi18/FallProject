class Sample2 {

    static int a2 = 10; // field

    public static void main(String[] args) {
        int a;        // unused -> should be reported
        int x = 0;    // used

        {
            int a2 = 1;              // shadows field a2
            System.out.println(a2);  // uses inner a2
        }

        System.out.println(x);
    }
}