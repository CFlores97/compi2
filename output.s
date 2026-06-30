.data
.align 2
str_1: .asciiz "a["
.align 2
str_2: .asciiz "] = "
.align 2
str_3: .asciiz "Gracias por usar Mini‑C!\n"
.align 2
g_m: .space 200
.align 2
g_a: .space 200

.text
.globl fill
.globl main

  j main
  nop

fill:
  # Prologo de fill
  addiu $sp, $sp, -88
  sw $ra, 84($sp)
  sw $fp, 80($sp)
  move $fp, $sp
  sw $a0, 0($fp)
  sw $a1, 4($fp)
  li $t0, 1
  sw $t0, 16($fp)
  lw $t0, 0($fp)
  sw $t0, 8($fp)
for_start_1:
  lw $t0, 8($fp)
  li $t1, 1
  slt $t2, $t0, $t1
  xori $t2, $t2, 1
  sw $t2, 20($fp)
  lw $t0, 20($fp)
  beq $t0, $zero, for_end_2
  nop
  lw $t0, 4($fp)
  sw $t0, 12($fp)
for_start_3:
  lw $t0, 12($fp)
  li $t1, 1
  slt $t2, $t0, $t1
  xori $t2, $t2, 1
  sw $t2, 24($fp)
  lw $t0, 24($fp)
  beq $t0, $zero, for_end_4
  nop
  lw $t0, 0($fp)
  lw $t1, 4($fp)
  sub $t2, $t0, $t1
  sw $t2, 28($fp)
  lw $t0, 16($fp)
  lw $t1, 28($fp)
  add $t2, $t0, $t1
  sw $t2, 32($fp)
  lw $t0, 32($fp)
  li $t1, 5
  add $t2, $t0, $t1
  sw $t2, 36($fp)
  lw $t0, 36($fp)
  li $t1, 15
  div $t0, $t1
  mfhi $t2
  sw $t2, 40($fp)
  la $t0, g_m
  sw $t0, 44($fp)
  lw $t0, 8($fp)
  li $t1, 5
  mul $t2, $t0, $t1
  sw $t2, 48($fp)
  lw $t0, 48($fp)
  lw $t1, 12($fp)
  add $t2, $t0, $t1
  sw $t2, 52($fp)
  lw $t0, 52($fp)
  li $t1, 4
  mul $t2, $t0, $t1
  sw $t2, 56($fp)
  lw $t0, 44($fp)
  lw $t1, 56($fp)
  add $t2, $t0, $t1
  sw $t2, 60($fp)
  lw $t0, 60($fp)
  lw $t1, 40($fp)
  sw $t1, 0($t0)
  lw $t0, 16($fp)
  li $t1, 1
  add $t2, $t0, $t1
  sw $t2, 64($fp)
  lw $t0, 64($fp)
  sw $t0, 16($fp)
  lw $t0, 12($fp)
  li $t1, 1
  sub $t2, $t0, $t1
  sw $t2, 68($fp)
  lw $t0, 68($fp)
  sw $t0, 12($fp)
  j for_start_3
  nop
for_end_4:
  lw $t0, 8($fp)
  li $t1, 1
  sub $t2, $t0, $t1
  sw $t2, 72($fp)
  lw $t0, 72($fp)
  sw $t0, 8($fp)
  j for_start_1
  nop
for_end_2:
  li $v0, 0
  j __end_fill
  nop

__end_fill:
  # Epilogo de fill
  move $sp, $fp
  lw $ra, 84($sp)
  lw $fp, 80($sp)
  addiu $sp, $sp, 88
  jr $ra
  nop

main:
  # Prologo de main
  addiu $sp, $sp, -120
  sw $ra, 116($sp)
  sw $fp, 112($sp)
  move $fp, $sp
  li $t0, 10
  sw $t0, 8($fp)
  li $t0, 5
  sw $t0, 12($fp)
  li $t0, 1
  sw $t0, 16($fp)
  li $t0, 50
  sw $t0, 20($fp)
  lw $a0, 8($fp)
  lw $a1, 12($fp)
  jal fill
  nop
  li $t0, 1
  sw $t0, 16($fp)
  li $t0, 1
  sw $t0, 0($fp)
for_start_5:
  lw $t0, 0($fp)
  lw $t1, 8($fp)
  slt $t2, $t1, $t0
  xori $t2, $t2, 1
  sw $t2, 24($fp)
  lw $t0, 24($fp)
  beq $t0, $zero, for_end_6
  nop
  li $t0, 1
  sw $t0, 4($fp)
for_start_7:
  lw $t0, 4($fp)
  lw $t1, 12($fp)
  slt $t2, $t1, $t0
  xori $t2, $t2, 1
  sw $t2, 28($fp)
  lw $t0, 28($fp)
  beq $t0, $zero, for_end_8
  nop
  la $t0, g_m
  sw $t0, 32($fp)
  lw $t0, 0($fp)
  li $t1, 5
  mul $t2, $t0, $t1
  sw $t2, 36($fp)
  lw $t0, 36($fp)
  lw $t1, 4($fp)
  add $t2, $t0, $t1
  sw $t2, 40($fp)
  lw $t0, 40($fp)
  li $t1, 4
  mul $t2, $t0, $t1
  sw $t2, 44($fp)
  lw $t0, 32($fp)
  lw $t1, 44($fp)
  add $t2, $t0, $t1
  sw $t2, 48($fp)
  lw $t0, 48($fp)
  lw $t1, 0($t0)
  sw $t1, 64($fp)
  la $t0, g_a
  sw $t0, 52($fp)
  lw $t0, 16($fp)
  li $t1, 4
  mul $t2, $t0, $t1
  sw $t2, 56($fp)
  lw $t0, 52($fp)
  lw $t1, 56($fp)
  add $t2, $t0, $t1
  sw $t2, 60($fp)
  lw $t0, 60($fp)
  lw $t1, 64($fp)
  sw $t1, 0($t0)
  lw $t0, 16($fp)
  li $t1, 1
  add $t2, $t0, $t1
  sw $t2, 68($fp)
  lw $t0, 68($fp)
  sw $t0, 16($fp)
  lw $t0, 4($fp)
  li $t1, 1
  add $t2, $t0, $t1
  sw $t2, 72($fp)
  lw $t0, 72($fp)
  sw $t0, 4($fp)
  j for_start_7
  nop
for_end_8:
  lw $t0, 0($fp)
  li $t1, 1
  add $t2, $t0, $t1
  sw $t2, 76($fp)
  lw $t0, 76($fp)
  sw $t0, 0($fp)
  j for_start_5
  nop
for_end_6:
  li $t0, 1
  sw $t0, 16($fp)
while_start_9:
  lw $t0, 20($fp)
  li $t1, 1
  add $t2, $t0, $t1
  sw $t2, 80($fp)
  lw $t0, 16($fp)
  lw $t1, 80($fp)
  xor $t2, $t0, $t1
  sltu $t2, $zero, $t2
  sw $t2, 84($fp)
  lw $t0, 84($fp)
  beq $t0, $zero, while_end_10
  nop
  la $a0, str_1
  jal print_str
  nop
  lw $a0, 16($fp)
  jal print_int
  nop
  la $a0, str_2
  jal print_str
  nop
  la $t0, g_a
  sw $t0, 88($fp)
  lw $t0, 16($fp)
  li $t1, 4
  mul $t2, $t0, $t1
  sw $t2, 92($fp)
  lw $t0, 88($fp)
  lw $t1, 92($fp)
  add $t2, $t0, $t1
  sw $t2, 96($fp)
  lw $t0, 96($fp)
  lw $t1, 0($t0)
  sw $t1, 100($fp)
  lw $a0, 100($fp)
  jal print_int
  nop
  jal println
  nop
  lw $t0, 16($fp)
  li $t1, 1
  add $t2, $t0, $t1
  sw $t2, 104($fp)
  lw $t0, 104($fp)
  sw $t0, 16($fp)
  j while_start_9
  nop
while_end_10:
  la $a0, str_3
  jal print_str
  nop
  li $v0, 0
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
