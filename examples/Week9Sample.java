class Week9Sample {

    int x = 0; // field

    void test() {
        int x = 1;      // shadows field x -> INFO (shadowing)
        int y = 5;
        int y = 6;      // redeclaration in same scope -> WARNING
    }
}