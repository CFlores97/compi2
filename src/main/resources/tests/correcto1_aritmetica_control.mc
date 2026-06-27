int main() {
    int x;
    int suma;

    x = 1;
    suma = 0;

    while (x <= 5) {
        suma = suma + x;
        x = x + 1;
    }

    if (suma == 15) {
        print_int(suma);
        println();
    } else {
        print_int(0);
        println();
    }

    return suma;
}