int matriz[2][3];
string mensaje;

int main() {
    int x;
    int i;
    int *p;

    x = 4;
    p = &x;

    *p = *p + 1;

    matriz[1][2] = x + 1;

    for (i = 0; i < 3; i = i + 1) {
        print_int(i);
        println();
    }

    do {
        x = x - 1;
    } while (x > 0);

    mensaje = "Prueba completa";
    print_str(mensaje);
    println();

    return matriz[1][2];
}