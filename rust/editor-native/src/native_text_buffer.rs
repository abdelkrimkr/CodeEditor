extern crate android_logger;
extern crate log;
use std::{fs::File, io::BufReader};

use android_logger::Config;
use log::{LevelFilter, debug};

use jni::{
    JNIEnv,
    objects::{JClass, JString},
    sys::{jchar, jint, jlong, jstring},
};
use ropey::Rope;

fn rope_from_ptr<'a>(ptr: jlong) -> &'a mut Rope {
    unsafe { &mut *(ptr as *mut Rope) }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_createRope(
    mut env: JNIEnv,
    _class: JClass,
    text: JString,
) -> jlong {
    android_logger::init_once(
        Config::default()
            .with_max_level(LevelFilter::Trace)
            .with_tag("NativeTextBuffer"),
    );

    let text: String = env.get_string(&text).unwrap().into();
    let rope = Box::new(Rope::from_str(&text));
    Box::into_raw(rope) as jlong
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_createRopeFromFile(
    mut env: JNIEnv,
    _class: JClass,
    jpath: JString,
) -> jlong {
    android_logger::init_once(
        Config::default()
            .with_max_level(LevelFilter::Trace)
            .with_tag("NativeTextBuffer"),
    );

    let path: String = match env.get_string(&jpath) {
        Ok(p) => p.into(),
        Err(_) => return 0,
    };

    debug!("Path {}", path);

    let file = match File::open(&path) {
        Ok(f) => f,
        Err(_) => return 0,
    };

    let rope = match Rope::from_reader(BufReader::new(file)) {
        Ok(r) => Box::new(r),
        Err(_) => return 0,
    };

    Box::into_raw(rope) as jlong
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_deleteRope(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
) {
    if ptr != 0 {
        unsafe {
            drop(Box::from_raw(ptr as *mut Rope));
        }
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeLen(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
) -> jint {
    rope_from_ptr(ptr).len_chars() as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeLineCount(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
) -> jint {
    rope_from_ptr(ptr).len_lines() as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeInsert(
    mut env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    idx: jint,
    text: JString,
) {
    let text: String = env.get_string(&text).unwrap().into();
    rope_from_ptr(ptr).insert(idx as usize, &text);
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeRemove(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    start: jint,
    end: jint,
) {
    rope_from_ptr(ptr).remove(start as usize..end as usize);
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeSlice(
    env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    start: jint,
    end: jint,
) -> jstring {
    let rope = rope_from_ptr(ptr);
    let slice = rope.slice(start as usize..end as usize).to_string();
    env.new_string(slice).unwrap().into_raw()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeLineToChar(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    line: jint,
) -> jint {
    rope_from_ptr(ptr).line_to_char(line as usize) as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeCharToLine(
    _env: JNIEnv,
    _class: JClass,
    ptr: jlong,
    char_idx: jint,
) -> jint {
    rope_from_ptr(ptr).char_to_line(char_idx as usize) as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeToString(
    env: JNIEnv,
    _class: JClass,
    ptr: jlong,
) -> jstring {
    let rope = rope_from_ptr(ptr);
    env.new_string(rope.to_string()).unwrap().into_raw()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeGetChar(
    _env: JNIEnv,
    _: JClass,
    ptr: jlong,
    index: jint,
) -> jchar {
    let rope = rope_from_ptr(ptr);
    rope.char(index as usize) as u16
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeGetLine(
    env: JNIEnv,
    _: JClass,
    ptr: jlong,
    line_index: jint,
) -> jstring {
    let rope = rope_from_ptr(ptr);

    let line = rope.line(line_index as usize);
    let line_str: String = line.chars().collect();

    env.new_string(line_str).unwrap().into_raw()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeByteLen(
    _: JNIEnv,
    _: JClass,
    ptr: jlong,
) -> jint {
    let rope = rope_from_ptr(ptr);
    rope.bytes().len() as jint
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_itsvks_code_core_NativeTextBuffer_ropeLineLen(
    _: JNIEnv,
    _: JClass,
    ptr: jlong,
    line: jint,
) -> jint {
    let rope = rope_from_ptr(ptr);
    rope.line(line as usize).len_chars() as jint
}
