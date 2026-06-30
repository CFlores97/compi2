int main() {
    int a;
    int b;
    int resultado;

    a = 10;
    b = 3;
    resultado = a + b * 2;

    if (resultado > 15) {
        print_int(resultado);
        println();
    } else {
        print_int(0);
        println();
    }

    return resultado;
}