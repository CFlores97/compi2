.data

.text
.globl main

main:
  # Prologo de main
  addiu $sp, $sp, -32
  sw $ra, 28($sp)
  sw $fp, 24($sp)
  move $fp, $sp
  li $t0, 1
  sw $t0, 0($fp)
  li $t0, 0
  sw $t0, 4($fp)
while_start_1:
  lw $t0, 0($fp)
  li $t1, 5
  slt $t2, $t1, $t0
  xori $t2, $t2, 1
  sw $t2, 8($fp)
  lw $t0, 8($fp)
  beq $t0, $zero, while_end_2
  nop
  lw $t0, 4($fp)
  lw $t1, 0($fp)
  add $t2, $t0, $t1
  sw $t2, 12($fp)
  lw $t0, 12($fp)
  sw $t0, 4($fp)
  lw $t0, 0($fp)
  li $t1, 1
  add $t2, $t0, $t1
  sw $t2, 16($fp)
  lw $t0, 16($fp)
  sw $t0, 0($fp)
  j while_start_1
  nop
while_end_2:
  lw $t0, 4($fp)
  li $t1, 15
  xor $t2, $t0, $t1
  sltiu $t2, $t2, 1
  sw $t2, 20($fp)
  lw $t0, 20($fp)
  beq $t0, $zero, if_else_3
  nop
  lw $a0, 4($fp)
  jal print_int
  nop
  jal println
  nop
  j if_end_4
  nop
if_else_3:
  li $a0, 0
  jal print_int
  nop
  jal println
  nop
if_end_4:
  lw $v0, 4($fp)
  j __end_main
  nop
  li $v0, 0
  j __end_main
  nop

__end_main:
  # Epilogo de main
  move $sp, $fp
  lw $ra, 28($sp)
  lw $fp, 24($sp)
  addiu $sp, $sp, 32
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
