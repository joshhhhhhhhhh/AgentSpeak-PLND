cd ../PLANNERS/touist-service || exit
python3 -m server &
cd ../epistemic-reasoner || exit
npm start &
cd ../../PELEUS || exit
sed -i -e 's/\r$//' gradlew
./gradlew