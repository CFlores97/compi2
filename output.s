.data
.align 2
str_1: .asciiz "Mini-C fase 2"
.align 2
g_contador: .word 0
.align 2
g_valores: .space 20
.align 2
g_matriz: .space 24
.align 2
g_saludo: .word str_1

.text
.globl main

suma:
  # Prologo de suma
  addiu $sp, $sp, -24
  sw $ra, 20($sp)
  sw $fp, 16($sp)
  move $fp, $sp
  sw $a0, 0($fp)
  sw $a1, 4($fp)
  lw $t0, 0($fp)
  lw $t1, 4($fp)
  add $t2, $t0, $t1
  sw $t2, 8($fp)
  lw $v0, 8($fp)
  j __end_suma
  nop
  li $v0, 0
  j __end_suma
  nop

__end_suma:
  # Epilogo de suma
  move $sp, $fp
  lw $ra, 20($sp)
  lw $fp, 16($sp)
  addiu $sp, $sp, 24
  jr $ra
  nop

main:
  # Prologo de main
  addiu $sp, $sp, -120
  sw $ra, 116($sp)
  sw $fp, 112($sp)
  move $fp, $sp
  li $t0, 2
  sw $t0, 0($fp)
  li $t0, 3
  sw $t0, 4($fp)
  addiu $t0, $fp, 0
  sw $t0, 12($fp)
  lw $t0, 12($fp)
  sw $t0, 8($fp)
  lw $a0, 0($fp)
  lw $a1, 4($fp)
  jal suma
  nop
  sw $v0, 16($fp)
  lw $t0, 8($fp)
  lw $t1, 16($fp)
  sw $t1, 0($t0)
  la $t0, g_valores
  sw $t0, 20($fp)
  li $t0, 0
  sw $t0, 24($fp)
  lw $t0, 20($fp)
  lw $t1, 24($fp)
  add $t2, $t0, $t1
  sw $t2, 28($fp)
  lw $t0, 28($fp)
  lw $t1, 0($fp)
  sw $t1, 0($t0)
  la $t0, g_valores
  sw $t0, 32($fp)
  li $t0, 0
  sw $t0, 36($fp)
  lw $t0, 32($fp)
  lw $t1, 36($fp)
  add $t2, $t0, $t1
  sw $t2, 40($fp)
  lw $t0, 40($fp)
  lw $t1, 0($t0)
  sw $t1, 48($fp)
  lw $t0, 48($fp)
  li $t1, 1
  add $t2, $t0, $t1
  sw $t2, 44($fp)
  la $t0, g_matriz
  sw $t0, 52($fp)
  li $t0, 3
  sw $t0, 56($fp)
  lw $t0, 56($fp)
  li $t1, 2
  add $t2, $t0, $t1
  sw $t2, 60($fp)
  lw $t0, 60($fp)
  li $t1, 4
  mul $t2, $t0, $t1
  sw $t2, 64($fp)
  lw $t0, 52($fp)
  lw $t1, 64($fp)
  add $t2, $t0, $t1
  sw $t2, 68($fp)
  lw $t0, 68($fp)
  lw $t1, 44($fp)
  sw $t1, 0($t0)
  li $t0, 0
  sw $t0, g_contador
for_start_1:
  lw $t0, g_contador
  li $t1, 3
  slt $t2, $t0, $t1
  sw $t2, 72($fp)
  lw $t0, 72($fp)
  beq $t0, $zero, for_end_2
  nop
  lw $a0, g_contador
  jal print_int
  nop
  jal println
  nop
  lw $t0, g_contador
  li $t1, 1
  add $t2, $t0, $t1
  sw $t2, 76($fp)
  lw $t0, 76($fp)
  sw $t0, g_contador
  j for_start_1
  nop
for_end_2:
do_start_3:
  lw $t0, 0($fp)
  li $t1, 1
  sub $t2, $t0, $t1
  sw $t2, 80($fp)
  lw $t0, 80($fp)
  sw $t0, 0($fp)
  lw $t0, 0($fp)
  li $t1, 0
  slt $t2, $t1, $t0
  sw $t2, 84($fp)
  lw $t0, 84($fp)
  beq $t0, $zero, do_end_4
  nop
  j do_start_3
  nop
do_end_4:
  lw $a0, g_saludo
  jal print_str
  nop
  jal println
  nop
  la $t0, g_matriz
  sw $t0, 88($fp)
  li $t0, 3
  sw $t0, 92($fp)
  lw $t0, 92($fp)
  li $t1, 2
  add $t2, $t0, $t1
  sw $t2, 96($fp)
  lw $t0, 96($fp)
  li $t1, 4
  mul $t2, $t0, $t1
  sw $t2, 100($fp)
  lw $t0, 88($fp)
  lw $t1, 100($fp)
  add $t2, $t0, $t1
  sw $t2, 104($fp)
  lw $t0, 104($fp)
  lw $t1, 0($t0)
  sw $t1, 108($fp)
  lw $v0, 108($fp)
  j __end_main
  nop
  li $v0, 0
  j __end_main
  nop

__end_main:
  # Epilogo de main
  move $sp, $fp
  lw $ra, 116($sp)
  lw $fp, 112($sp)
  addiu $sp, $sp, 120
  li $v0, 10
  syscall



# ===== RUNTIME =====

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
