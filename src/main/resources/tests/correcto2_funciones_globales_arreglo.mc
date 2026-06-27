int contador;
int valores[3];

int suma(int a, int b) {
    return a + b;
}

int main() {
    int resultado;

    contador = 10;
    valores[0] = 2;
    valores[1] = 3;

    resultado = suma(valores[0], valores[1]);

    print_int(contador);
    println();

    print_int(resultado);
    println();

    return resultado;
}