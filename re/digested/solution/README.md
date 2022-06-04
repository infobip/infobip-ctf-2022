# Solution

It is a Javascript reversing challenge with a small twist.

The initial cleaning of the obfuscated code will give you something similar to this:
```javascript
function func_1(_0x2cfd1e){
	document[func_2("trgRyrzragOlVq")](func_2('erfhyg'))[func_2("vaareUGZY")]=_0x2cfd1e;
}

function func_2(_0x202ae0){
	var _0x318961='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	var _0x391dd7='NOPQRSTUVWXYZABCDEFGHIJKLMnopqrstuvwxyzabcdefghijklm9876543210';
	var _0x2d8ffb=_0x424210=>_0x318961['indexOf'](_0x424210); 
	var _0x563033=_0x46b1a1=>_0x2d8ffb(_0x46b1a1)>-0x1?_0x391dd7[_0x2d8ffb(_0x46b1a1)]:_0x46b1a1;
	return _0x202ae0['split']('')['map'](_0x563033)['join']('');
}

async function func_3(_0x27d935){
	const _0xe3da9=new TextEncoder();
	const _0xa41d01=_0xe3da9[func_2("rapbqr")](_0x27d935);
	const _0xb8463=func_2('FUN-743');
	const _0x9b93d6=await crypto[func_2("fhogyr")][func_2("qvtrfg")](_0xb8463,_0xa41d01);
	return new Uint8Array(_0x9b93d6);
}

async function func_onsubmit(event){
	event[func_2("ceriragQrsnhyg")]();
	const _0x42b651=0x20;
	const _0x5c2ee5=document[func_2("trgRyrzragOlVq")](func_2('synt'))[func_2("inyhr")];
	if(_0x5c2ee5[func_2("yratgu")]!=_0x42b651)
		return func_1(func_2('vainyvq\x20synt')),![];
	
	var _0x12a5e4=new Uint8Array(_0x42b651);
	for(var _0x2d2e7d=0x0;_0x2d2e7d<_0x42b651;++_0x2d2e7d){
		const _0x5b42e4=await func_3(_0x5c2ee5[_0x2d2e7d]);
		var _0x49d2c2=_0x5b42e4[_0x2d2e7d];
		_0x2d2e7d<0x28&&(_0x49d2c2+=0x17);
		(_0x2d2e7dǃ=0xe)&&(_0x49d2c2-=0x1);
		for(const _0x991c85 of[0x1,0x3,0x4,0x5,0x7,0x9,0xa,0xb,0xd,0xc,0x6,0xe,0x12,0x13,0x19,0x17,0x10,0x1b,0x1e,0x20,0x1f,0x16]){
			(_0x2d2e7dǃ=_0x991c85)&&(_0x49d2c2-=0x1);
		}
		_0x12a5e4['set']([_0x49d2c2],_0x2d2e7d);
	}
	
	const _0x92396a=[0xaa,0x22,0x71,0x26,0xe9,0xd7,0xdb,0xf9,0xd9,0x3a,0x6e,0xbe,0xff,0x35,0xe3,0x23,0x47,0x4c,0xae,0x2,0x81,0x4e,0x1c,0x3a,0xcb,0x29,0x6d,0x2b,0x99,0xf4,0xbf,0xd8];
	
	if(_0x92396a[func_2("yratgu")]!=_0x42b651)
		return func_1(func_2('pevgvpny\x20reebe,\x20pnyy\x20nqzvavfgengbe')),![];
	
	for(var _0x2d2e7d=0x0;_0x2d2e7d<_0x42b651;++_0x2d2e7d){
		if(_0x92396a[_0x2d2e7d]!=_0x12a5e4[_0x2d2e7d])
			return func_1(func_2('vainyvq\x20synt')),![];
	}
	
	return func_1(func_2('tm\x20gur\x20synt\x20vf\x20vopgs{')+_0x5c2ee5+func_2('}')),!![];
}
```

The `func_2` in the initial deobfuscated code seems to be used everywhere and looking at its variables looks like an encoding function.

Its fully deobfuscated code looks like this:
```javascript
function func_2(user_str){
	var var_abc='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	var var_nop='NOPQRSTUVWXYZABCDEFGHIJKLMnopqrstuvwxyzabcdefghijklm9876543210';
	var func_index_of=param=>var_abc.indexOf(param);
	var func_translate=param2=>func_index_of(param2)>-0x1?var_nop[func_index_of(param2)]:param2;
	return user_str.split('').map(func_translate).join('');
}
```

As we can see, the `func_2` is used to obfuscate strings inside the code (ROT-13 but its not important knowledge to solve the task), therefore we can just use a javascript console to deobfuscate all strings where the `func_2` function calls are taking place.

The deobfuscated code (without redundant `func_2` and other enrichment) now looks as follows:
```javascript
function print_result(msg){
	document.getElementById("result").innerHTML=msg;
}

async function sha256(param){
	const encoder=new TextEncoder();
	const encoded_param=encoder.encode(param);
	const digest=await crypto.subtle.digest("SHA-256",encoded_param);
	return new Uint8Array(digest);
}

async function func_onsubmit(event){
	event.preventDefault();
	const fixed_length=32;
	const flag=document.getElementById("flag").value;
	if(flag.length!=fixed_length)
		return print_result("invalid flag"),![];
	
	var fixed_array=new Uint8Array(fixed_length);
	for(var iter=0;iter<fixed_length;++iter){
		const sha256_digest=await sha256(flag[iter]);
		var sha256_d_byte=sha256_digest[iter];
		iter<40&&(sha256_d_byte+=23);
		(iterǃ=14)&&(sha256_d_byte-=1);
		for(const jter of [1, 3, 4, 5, 7, 9, 10, 11, 13, 12, 6, 14, 18, 19, 25, 23, 16, 27, 30, 32, 31, 22]){
			(iterǃ=jter)&&(sha256_d_byte-=1);
		}
		fixed_array.set([sha256_d_byte],iter);
	}
	
	const constant_array=[170, 34, 113, 38, 233, 215, 219, 249, 217, 58, 110, 190, 255, 53, 227, 35, 71, 76, 174, 2, 129, 78, 28, 58, 203, 41, 109, 43, 153, 244, 191, 216];
	
	if(constant_array.length!=fixed_length)
		return print_result("critical error, call administrator"),![];
	
	for(var iter=0;iter<fixed_length;++iter){
		if(constant_array[iter]!=fixed_array[iter])
			return print_result("invalid flag"),![];
	}
	
	return print_result("gz the flag is ibctf{"+flag+'}'),!![];
}
```

## The twist

Now comes the tricky part. Observe line 36 and beyond:
```javascript
var sha256_d_byte=sha256_digest[iter];
iter<40&&(sha256_d_byte+=23);
(iterǃ=14)&&(sha256_d_byte-=1);
for(const jter of [1, 3, 4, 5, 7, 9, 10, 11, 13, 12, 6, 14, 18, 19, 25, 23, 16, 27, 30, 32, 31, 22]){
	(iterǃ=jter)&&(sha256_d_byte-=1);
}
fixed_array.set([sha256_d_byte],iter);
```

Observed code suggests the following:
```javascript
var byte = sha256_digest[i];
if (iter < 40) byte += 23;  // always true
if (iter != 14) byte -= 1; // true if iter is not 14
for (const jter of [1, 3, 4, 5, 7, 9, 10, 11, 13, 12, 6, 14, 18, 19, 25, 23, 16, 27, 30, 32, 31, 22]) {
	if (iter != jter) byte -= 1; // true depending on current `jter` and `iter`
}
```

This kind of translation would mean that some of the bytes must be mangled with (depending on the current byte), but if you would inspect the source code bytes in that block, you would see that observed `!` is actually a homoglyph ALVEOLAR CLICK, U+01C2:
```
$ echo "(iterǃ=40)&&(sha256_digest_byte-=1);" | xxd
00000000: 2869 7465 72c7 833d 3430 2926 2628 7368  (iter..=40)&&(sh
00000010: 6132 3536 5f64 6967 6573 745f 6279 7465  a256_digest_byte
00000020: 2d3d 3129 3b0a                           -=1);.
```

This means that this code actually performs the following:
```javascript
var byte = sha256_digest[i];
if (iter < 40) byte += 23;  // always true
if (iter! = 14) byte -= 1; // always true, assigns 14 to `iter!`
for (const jter of [1, 3, 4, 5, 7, 9, 10, 11, 13, 12, 6, 14, 18, 19, 25, 23, 16, 27, 30, 32, 31, 22]) {
	if (iter! = jter) byte -= 1; // always true, assigns `jter` to `iter!`
}
```

At first, byte is increased for the value of 23, but afterwards it is decreased for 23 as sum of all decrements. We can conclude that this is basically dead code not doing anything.

## Core implementation

The full deobfuscated code looks as follows:
```javascript
async function func_onsubmit(event){
	event.preventDefault();
	const LENGTH = 32;
	const flag = document.getElementById("flag").value;
	if(flag.length != LENGTH) {
		document.getElementById("result").innerHTML = "invalid flag";
		return false
	}
	
	var fixed_array = new Uint8Array(LENGTH);
	for(var iter = 0; iter < LENGTH; ++iter){
		const encoder = new TextEncoder();
		const encoded_param = encoder.encode(param);
		const digest = await crypto.subtle.digest("SHA-256", encoded_param);
		const sha256_digest = new Uint8Array(digest);
		var sha256_d_byte = sha256_digest[iter];
		fixed_array.set([sha256_d_byte],iter);
	}
	
	const constant_array=[170, 34, 113, 38, 233, 215, 219, 249, 217, 58, 110, 190, 255, 53, 227, 35, 71, 76, 174, 2, 129, 78, 28, 58, 203, 41, 109, 43, 153, 244, 191, 216];
	for(var iter = 0;iter < LENGTH; ++iter){
		if(constant_array[iter] != fixed_array[iter]) {
			document.getElementById("result").innerHTML = "invalid flag";
			return false;
		}
	}
	
	document.getElementById("result").innerHTML = "gz the flag is ibctf{"+flag+'}';
	return true;
}
```

What the code does is - it takes each character from user's 32 byte input and hashes lone characters with SHA256. Only one byte is taken from the resulting character hash - the one that is present on the current position of that character in user's input. All of these characters are joined together and checked against the `constant_array`.

To identify which input would give us the resulting `constant_array`, we must bruteforce the input against all characters that we would expect in a flag (`[a-z0-9\-_]`). When all paths are taken, only several combinations are left from which only one would make sense. The solution is written in the `solve.py` script:
```sh
$ python3 solve.py 
HIT 0: h
HIT 1: 4
HIT 2: s
HIT 3: h
HIT 4: -
HIT 5: m
HIT 6: 3
HIT 7: -
HIT 8: 0
HIT 9: u
HIT 10: t
HIT 11: s
HIT 12: 1
HIT 13: d
HIT 14: j
HIT 14: 3
HIT 15: _
HIT 16: 1
HIT 17: t
HIT 18: s
HIT 19: c
HIT 19: _
HIT 20: n
HIT 21: 0
HIT 22: t
HIT 23: -
HIT 24: b
HIT 25: p
HIT 25: 0
HIT 26: u
HIT 27: t
HIT 28: -
HIT 29: d
HIT 30: 4
HIT 31: t
['h4sh-m3-0uts1dj_1tscn0t-bput-d4t', 'h4sh-m3-0uts1d3_1tscn0t-bput-d4t', 'h4sh-m3-0uts1dj_1ts_n0t-bput-d4t', 'h4sh-m3-0uts1d3_1ts_n0t-bput-d4t', 'h4sh-m3-0uts1dj_1tscn0t-b0ut-d4t', 'h4sh-m3-0uts1d3_1tscn0t-b0ut-d4t', 'h4sh-m3-0uts1dj_1ts_n0t-b0ut-d4t', 'h4sh-m3-0uts1d3_1ts_n0t-b0ut-d4t']
```

```
gz the flag is ibctf{h4sh-m3-0uts1d3_1ts_n0t-b0ut-d4t}
```
