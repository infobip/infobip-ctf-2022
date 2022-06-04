mod storage;

use rand;
use rand::Rng;
use std::io::{Read, Write};
use std::net::{Shutdown, TcpListener, TcpStream};
use std::time::Duration;
use std::thread;
use crate::storage::ERR_DEVICE;
use std::thread::sleep;

macro_rules! authenticate {
    ($self:ident) => {
        if !$self.check_session() {
            $self.send_err(storage::ERR_MESSAGE, "bad session\0\0\0");
            return;
        }
    };
}

#[repr(C, packed)]
#[derive(Debug, Copy, Clone)]
struct M1N1Req {
    rq_b: u8,
    sess_tkt: [u8; 5],
    op_cat: u8,
    op_seq: u8,
    data: [u8; 8],
}

struct M1N1ReqHandler<'a> {
    req: Option<M1N1Req>,
    stream: &'a TcpStream,
}

impl M1N1ReqHandler<'_> {
    fn send_err(&mut self, which: u8, desc: &str) {
        self.stream.write(&[storage::STATUS_ERR, which]);
        self.stream.write(&desc.as_bytes());
        self.stream.flush();
    }

    fn init_session(&mut self) {
        let mut rng = rand::thread_rng();
        let pos = rng.gen_range(0..storage::SESS_TKTS.len());
        self.stream.write(&[storage::STATUS_OK]);
        self.stream.write(&storage::SESS_TKTS[pos]);
        self.stream.write(&rand::thread_rng().gen::<[u8; 10]>());
        self.stream.flush();
    }

    fn check_session(&mut self) -> bool {
        let r = self.req.unwrap();
        for tkts in storage::SESS_TKTS {
            if tkts.iter().zip(r.sess_tkt.iter()).all(|(s1, s2)| s1 == s2) {
                return true;
            }
        }
        return false;
    }

    fn destroy_session(&mut self) {
        authenticate!(self);
        let r = self.req.unwrap();
        self.stream.write(&[storage::STATUS_OK]);
        self.stream.write(&r.sess_tkt);
        self.stream.write(&rand::thread_rng().gen::<[u8; 10]>());
        self.stream.flush();
    }

    fn read_sector(&mut self) {
        authenticate!(self);
        let r = self.req.unwrap();
        let sect_addr = (r.data[0] as u16) << 8 | r.data[1] as u16;

        let sector = match sect_addr {
            storage::S2_SC1_ADDR => storage::SLOT_2_SECT_1,
            storage::S2_SC2_ADDR => storage::SLOT_2_SECT_2,
            storage::S3_SC1_ADDR => storage::SLOT_3_SECT_1,
            storage::S3_SC2_ADDR => storage::SLOT_3_SECT_2,
            storage::S3_SC3_ADDR => storage::SLOT_3_SECT_3,
            storage::S5_SC1_ADDR => storage::SLOT_5_SECT_1,
            storage::S5_SC2_ADDR => storage::SLOT_5_SECT_2,
            storage::S5_SC3_ADDR => storage::SLOT_5_SECT_3,
            _ => {
                self.stream.write(&[storage::STATUS_ERR, ERR_DEVICE]);
                self.stream.write(b"unknown sector");
                self.stream.flush();
                return;
            },
        };

        self.stream.write(&sector);
        self.stream.flush();
    }

    fn read_slot(&mut self) {
        authenticate!(self);
        self.stream.write(&storage::ENTRY_SLOTS);
        self.stream.flush();
    }

    fn serve_req(&mut self) {
        let r = self.req.unwrap();
        match (r.op_cat, r.op_seq) {
            (storage::OP_SESS_TKT_CAT, storage::OP_SESS_TKT_INIT_SEQ) => self.init_session(),
            (storage::OP_SESS_TKT_CAT, storage::OP_SESS_TKT_DEST_SEQ) => self.destroy_session(),
            (storage::OP_READ_CAT, storage::OP_READ_SLOT_SEQ) => self.read_slot(),
            (storage::OP_READ_CAT, storage::OP_READ_SECT_SEQ) => self.read_sector(),
            (_, _) => self.send_err(storage::ERR_MESSAGE, "unknown op c/s"),
        }
    }
}

fn handle_conn(mut stream: &TcpStream) {
    let mut attempt = 0;
    let mut buffer = [0; 16];
    stream.set_read_timeout(Option::Some(Duration::from_secs(3)));
    stream.set_write_timeout(Option::Some(Duration::from_secs(3)));

    while match stream.read(&mut buffer) {
        Ok(n) => {
            if n != 0 {
                let (_, req, _) = unsafe { buffer.align_to::<M1N1Req>() };
                let mut handler = M1N1ReqHandler { req: None, stream };

                if req.len() > 0 && req[0].rq_b == storage::RQ_BYTE {
                    handler.req = Some(req[0]);
                    handler.serve_req();
                } else {
                    handler.send_err(storage::ERR_MESSAGE, "bad request\0\0\0");
                }
                true
            } else {
                sleep(Duration::from_millis(100));
                attempt += 1;
                attempt < 3
            }
        },
        Err(_) => {
            false
        }
    } {};
    stream.shutdown(Shutdown::Both);
}

fn main() {
    TcpListener::bind("0.0.0.0:9000")
        .unwrap()
        .incoming()
        .for_each(|stream| {
            thread::spawn(|| handle_conn(&stream.unwrap()));
        });
}
