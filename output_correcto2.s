.data
.align 2
g_contador: .word 0
.align 2
g_valores: .space 12

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
  addiu $sp, $sp, -72
  sw $ra, 68($sp)
  sw $fp, 64($sp)
  move $fp, $sp
  li $t0, 10
  sw $t0, g_contador
  la $t0, g_valores
  sw $t0, 4($fp)
  li $t0, 0
  sw $t0, 8($fp)
  lw $t0, 4($fp)
  lw $t1, 8($fp)
  add $t2, $t0, $t1
  sw $t2, 12($fp)
  lw $t0, 12($fp)
  li $t1, 2
  sw $t1, 0($t0)
  la $t0, g_valores
  sw $t0, 16($fp)
  li $t0, 4
  sw $t0, 20($fp)
  lw $t0, 16($fp)
  lw $t1, 20($fp)
  add $t2, $t0, $t1
  sw $t2, 24($fp)
  lw $t0, 24($fp)
  li $t1, 3
  sw $t1, 0($t0)
  la $t0, g_valores
  sw $t0, 28($fp)
  li $t0, 0
  sw $t0, 32($fp)
  lw $t0, 28($fp)
  lw $t1, 32($fp)
  add $t2, $t0, $t1
  sw $t2, 36($fp)
  lw $t0, 36($fp)
  lw $t1, 0($t0)
  sw $t1, 40($fp)
  la $t0, g_valores
  sw $t0, 44($fp)
  li $t0, 4
  sw $t0, 48($fp)
  lw $t0, 44($fp)
  lw $t1, 48($fp)
  add $t2, $t0, $t1
  sw $t2, 52($fp)
  lw $t0, 52($fp)
  lw $t1, 0($t0)
  sw $t1, 56($fp)
  lw $a0, 40($fp)
  lw $a1, 56($fp)
  jal suma
  nop
  sw $v0, 60($fp)
  lw $t0, 60($fp)
  sw $t0, 0($fp)
  lw $a0, g_contador
  jal print_int
  nop
  jal println
  nop
  lw $a0, 0($fp)
  jal print_int
  nop
  jal println
  nop
  lw $v0, 0($fp)
  j __end_main
  nop
  li $v0, 0
  j __end_main
  nop

__end_main:
  # Epilogo de main
  move $sp, $fp
  lw $ra, 68($sp)
  lw $fp, 64($sp)
  addiu $sp, $sp, 72
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
