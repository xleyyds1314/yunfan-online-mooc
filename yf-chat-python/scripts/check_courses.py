import sys
sys.path.insert(0, '.')
from app.services import rag
h = rag.list_all_courses_in_rag()
print(f'total={len(h)}')
for c in h:
    print(f'  {c["course_id"]} {c["name"]}')
