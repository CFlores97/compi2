.text

# Runtime mínimo para Mini-C. Los argumentos llegan en $a0/$a1 y los retornos en $v0.

.globl print_int
print_int:
    li $v0, 1
    syscall
    jr $ra
    nop

.globl print_char
print_char:
    li $v0, 11
    syscall
    jr $ra
    nop

# Por decisión del proyecto, bool se imprime como 0 o 1.
.globl print_bool
print_bool:
    li $v0, 1
    syscall
    jr $ra
    nop

.globl print_str
print_str:
    li $v0, 4
    syscall
    jr $ra
    nop

.globl println
println:
    li $v0, 11
    li $a0, 10
    syscall
    jr $ra
    nop

.globl read_int
read_int:
    li $v0, 5
    syscall
    jr $ra
    nop

.globl read_char
read_char:
    li $v0, 12
    syscall
    jr $ra
    nop

# $a0 = buffer; $a1 = maxlen
.globl read_str
read_str:
    li $v0, 8
    syscall
    jr $ra
    nop
