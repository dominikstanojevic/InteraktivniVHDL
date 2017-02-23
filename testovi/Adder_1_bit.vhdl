ENTITY Adder_1_bit IS PORT(
	x: in std_logic;
	y: in std_logic;
	cin: in std_logic;
	sum: out std_logic;
	cout: out std_logic
);
END Adder_1_bit;

architecture arch OF Adder_1_bit IS
BEGIN
	sum <= x xor y xor cin;
	cout <= (x and y) or (x and cin) or (y and cin);
END arch;