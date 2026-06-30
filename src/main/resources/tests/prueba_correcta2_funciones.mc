int maximo(int a, int b) {
    if (a > b) {
        return a;
    } else {
        return b;
    }
}

int main() {
    int valores[3];
    int i;
    int max;

    valores[0] = 5;
    valores[1] = 12;
    valores[2] = 7;

    i = 0;
    max = 0;

    while (i < 3) {
        max = maximo(max, valores[i]);
        i = i + 1;
    }

    print_int(max);
    println();

    return max;
}