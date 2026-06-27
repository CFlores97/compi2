.text



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


# Recibe:
# $a0 = dirección del buffer
# $a1 = tamaño máximo
.globl read_str
read_str:
    li $v0, 8
    syscall
    jr $ra
    nop