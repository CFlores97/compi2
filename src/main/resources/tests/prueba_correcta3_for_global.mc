int acumulador;

int main() {
    int i;

    acumulador = 0;

    for (i = 1; i <= 5; i = i + 1) {
        acumulador = acumulador + i;
    }

    print_int(acumulador);
    println();

    return acumulador;
}