
test('multiply by 2', () => {
  expect(timesTwo(4)).toBe(8);
});

function timesTwo(num){
  return num*2;
}
