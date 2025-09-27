class Sample2 {
    public static void main(String[] args) {
        int a;
        // System.out.println(a); // would be use-before-init (commented)
        int a2 = 0;
        { int a2 = 1; } // shadowing in inner block
    }
}
