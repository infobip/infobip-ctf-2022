import socket

RQ = 0x52

STATUS_OK = 0x6f
STATUS_ERR = 0x77
ERR_MESSAGE = 0x6d
ERR_DEVICE = 0x63

SESSION_OP_CAT = 0x73
SESSION_OP_INIT_SEQ = 0x69
SESSION_OP_DEST_SEQ = 0x64

READ_OP_CAT = 0x72
READ_OP_SLOT_SEQ = 0x65
READ_OP_SECT_SEQ = 0x73

class M1N1Req:

    def __init__(self, op_cat, op_seq, sess_tkt=b"\x00"*5, data=b""):
        self.sess_tkt = sess_tkt
        self.op_cat = op_cat
        self.op_seq = op_seq
        self.data = data

    def to_bytes(self):
        return bytes([RQ]) + self.sess_tkt + bytes([self.op_cat]) + bytes([self.op_seq]) + self.data

class Slot:

    def __init__(self, bs):
        self.taken = bs[0] >> 7 == 1
        self.datasize = bs[0] & 127
        self.start_addr = bytes([bs[1], bs[2]])

    def dump(self, sock, sess_tkt):
        sectors = []
        m = M1N1Req(READ_OP_CAT, READ_OP_SECT_SEQ, sess_tkt, self.start_addr)
        sector = Sector(sr(sock, m))
        sectors.append(sector)
        while sector.has_next:
            m.data = sector.next_addr
            sector = Sector(sr(sock, m))
            sectors.append(sector)

        return sectors

    def __repr__(self):
        return str(self.__dict__)

    @staticmethod
    def from_slots(bs):
        return [Slot(bs[i+1:i+4]) for i in range(0, 15, 3)]

    @staticmethod
    def sector_data(sectors):
        return b''.join([sector.data for sector in sectors])


class Sector:

    def __init__(self, bs):
        self.locked = bs[1] & 0x20
        self.has_metadata = bs[1] & 0x10
        self.has_next = bs[1] & 0x08
        self.blocked = bs[1] & 0x04
        self.data = bs[2:10 if self.has_metadata else 14]
        self.metadata = bs[10:14] if self.has_metadata else []
        self.next_addr = bs[14:]

    def __repr__(self):
        return str(self.__dict__)


def sr(sock, req):
    print(f"s: {req.to_bytes()} => ", end='')
    sock.send(req.to_bytes())
    data = sock.recv(16)

    if data[0] != STATUS_OK:
        raise RuntimeError(f"{data[0]}, error: {data[1]}, optional msg: {data[2:]}")

    print(f"r: {data}")
    return data

if __name__ == "__main__":
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(("localhost", 9000))
    sess_tkt = sr(s, M1N1Req(SESSION_OP_CAT, SESSION_OP_INIT_SEQ))[1:6]
    print(f"ticket: {sess_tkt}")

    m = M1N1Req(READ_OP_CAT, READ_OP_SLOT_SEQ, sess_tkt=sess_tkt)
    slots = Slot.from_slots(sr(s, m))
    print(f"slot list: {slots}")

    m.op_seq = READ_OP_SECT_SEQ
    for slot in slots:
        if slot.taken:
            sectors = slot.dump(s, sess_tkt)
            print(f"slot data: {Slot.sector_data(sectors)}")

