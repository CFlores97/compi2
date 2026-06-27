int contador = 0;
int valores[5];
int matriz[2][3];
string saludo = "Mini-C fase 2";

int suma(int a, int b) {
    return a + b;
}

int main() {
    int x = 2;
    int y = 3;
    int *p;

    p = &x;
    *p = suma(x, y);

    valores[0] = x;
    matriz[1][2] = valores[0] + 1;

    for (contador = 0; contador < 3; contador = contador + 1) {
        print_int(contador);
        println();
    }

    do {
        x = x - 1;
    } while (x > 0);

    print_str(saludo);
    println();
    return matriz[1][2];
}
